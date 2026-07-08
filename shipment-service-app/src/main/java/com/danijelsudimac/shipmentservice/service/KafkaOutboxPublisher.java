package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        value = "app.kafka-publisher.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class KafkaOutboxPublisher {

    private static final String PUBLISHING_ERROR_MESSAGE = "Kafka publish failed";
    private static final String CLEANUP_MESSAGE = "Deleted {} published outbox events";
    private static final int MAX_RETRY_NUMBER = 3;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxEventRepository repository;
    private final ShipmentMetrics shipmentMetrics;

    @Scheduled(cron = "0 0 2 * * *")  // Every day at 2h AM
    public void cleanupOldEvents() {
        var deleted = repository.deletePublishedOlderThan(Instant.now().minus(7, ChronoUnit.DAYS));
        log.info(CLEANUP_MESSAGE, deleted);
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publish() {
        var events = repository.lockNextBatch(50);
        for (OutboxEvent event : events) {
            try {
                Object payload  = switch (event.getEventType()) {
                    case CREATE_SHIPMENT -> ShipmentCreatedEvent.parseFrom(event.getPayload());
                    case UPDATE_SHIPMENT -> ShipmentUpdatedEvent.parseFrom(event.getPayload());
                    case DELETE_SHIPMENT -> ShipmentDeletedEvent.parseFrom(event.getPayload());
                };

                kafkaTemplate.send(
                        event.getTopic(),
                        event.getAggregateId(),
                        payload
                ).whenComplete((res, ex) -> {
                    if (ex == null) {
                        repository.markPublished(event.getId(), Instant.now());
                        shipmentMetrics.incrementPublished();
                    } else {
                        log.error(PUBLISHING_ERROR_MESSAGE, ex);
                        if (event.getRetryCount() > MAX_RETRY_NUMBER) {
                            repository.markFailed(event.getId());
                            shipmentMetrics.incrementFailed();
                        } else {
                            repository.markRetired(event.getId(), event.getRetryCount() + 1);
                        }
                    }
                });
            } catch (Exception ex) {
                log.error(PUBLISHING_ERROR_MESSAGE, ex);
            }
        }
    }
}
