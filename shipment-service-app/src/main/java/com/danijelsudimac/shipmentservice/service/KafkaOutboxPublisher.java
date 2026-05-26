package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import com.danijelsudimac.shipmentservice.util.AvroUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!dev")
public class KafkaOutboxPublisher {

    private static final String PUBLISHING_ERROR_MESSAGE = "Kafka publish failed";
    public static final String SHIPMENT_INGEST_TOPIC = "shipment-ingest-topic.v1";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxEventRepository repository;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publish() {
        var events = repository.lockNextBatch(50);

        for (OutboxEvent event : events) {
            try {
                Object payload  = switch (OutboxEventType.valueOf(event.getEventType())) {
                    case CREATE_SHIPMENT -> AvroUtils.deserialize(event.getPayload(), ShipmentCreatedEvent.class);
                    case UPDATE_SHIPMENT -> AvroUtils.deserialize(event.getPayload(), ShipmentUpdatedEvent.class);
                    case DELETE_SHIPMENT -> AvroUtils.deserialize(event.getPayload(), ShipmentDeletedEvent.class);
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
                    }
                });
            } catch (Exception ex) {
                log.error(PUBLISHING_ERROR_MESSAGE, ex);
            }
        }
    }
}
