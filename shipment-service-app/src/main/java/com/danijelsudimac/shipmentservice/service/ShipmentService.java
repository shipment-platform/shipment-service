package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import com.danijelsudimac.shipmentservice.util.AvroUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;

import static com.danijelsudimac.shipmentservice.service.KafkaOutboxPublisher.SHIPMENT_INGEST_TOPIC;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private static final String AGGREGATE_TYPE = "Shipment";
    private final OutboxEventRepository outboxRepository;
    @Transactional
    public void processEvent(ShipmentCreatedEvent event) throws IOException {
        processEvent(event, OutboxEventType.CREATE_SHIPMENT, event.getExternalId());
    }

    @Transactional
    public void processEvent(ShipmentUpdatedEvent event) throws IOException {
        processEvent(event, OutboxEventType.UPDATE_SHIPMENT, event.getExternalId());
    }

    @Transactional
    public void processEvent(ShipmentDeletedEvent event) throws IOException {
        processEvent(event, OutboxEventType.DELETE_SHIPMENT, event.getExternalId());
    }

    @Transactional
    private void processEvent(Object event, OutboxEventType type, String externalId) throws IOException {
        OutboxEvent outbox =
                OutboxEvent.builder()
                        .aggregateType(AGGREGATE_TYPE)
                        .aggregateId(externalId)
                        .eventType(type.toString())
                        .topic(SHIPMENT_INGEST_TOPIC)
                        .payload(AvroUtils.serialize(event))
                        .published(false)
                        .createdAt(Instant.now())
                        .build();

        outboxRepository.save(outbox);
    }
}
