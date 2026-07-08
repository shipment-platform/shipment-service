package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.entity.OutboxEventStatus;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType;
import com.danijelsudimac.shipmentservice.repository.OutboxEventJPARepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;

@Service
public class ShipmentService {

    private static final String AGGREGATE_TYPE = "Shipment";
    private final OutboxEventJPARepository outboxJPARepository;
    private final ShipmentMetrics shipmentMetrics;
    private final String topicName;

    public ShipmentService(OutboxEventJPARepository outboxJPARepository,
                           ShipmentMetrics shipmentMetrics,
                           @Value("${application.kafka.shipment-topic}") String topicName) {
        this.outboxJPARepository = outboxJPARepository;
        this.shipmentMetrics = shipmentMetrics;
        this.topicName = topicName;
    }
    @Transactional
    public void processEvent(ShipmentCreatedEvent event) {
        processEvent(event.toByteArray(), OutboxEventType.CREATE_SHIPMENT, event.getExternalId());
    }

    @Transactional
    public void processEvent(ShipmentUpdatedEvent event) {
        processEvent(event.toByteArray(), OutboxEventType.UPDATE_SHIPMENT, event.getExternalId());
    }

    @Transactional
    public void processEvent(ShipmentDeletedEvent event) {
        processEvent(event.toByteArray(), OutboxEventType.DELETE_SHIPMENT, event.getExternalId());
    }

    @Transactional
    private void processEvent(byte[] event, OutboxEventType type, String externalId) {
        OutboxEvent outbox =
                OutboxEvent.builder()
                        .aggregateType(AGGREGATE_TYPE)
                        .aggregateId(externalId)
                        .eventType(type)
                        .topic(topicName)
                        .payload(event)
                        .status(OutboxEventStatus.PENDING)
                        .createdAt(Instant.now())
                        .build();
        outboxJPARepository.save(outbox);
        shipmentMetrics.incrementOutbox();
    }
}
