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
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxEventRepository repository;
    private final ShipmentMetrics shipmentMetrics;

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
                        event.setPublished(true);
                        event.setPublishedAt(Instant.now());
                        repository.save(event);
                        shipmentMetrics.incrementPublished();
                    } else {
                        log.error(PUBLISHING_ERROR_MESSAGE, ex);
                        shipmentMetrics.incrementFailed();
                    }
                });
            } catch (Exception ex) {
                log.error(PUBLISHING_ERROR_MESSAGE, ex);
            }
        }
    }
}
