package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShipmentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonService jsonService;

    public ShipmentEventPublisher(KafkaTemplate<String, String> kafkaTemplate, JsonService jsonService) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonService = jsonService;
    }

    public void publishShipmentCreateEvent(String topic, ShipmentCreatedEvent event) {
        kafkaTemplate.send(topic, jsonService.convertToJson(event));
    }

    public void publishShipmentUpdateEvent(String topic, ShipmentUpdatedEvent event) {
        kafkaTemplate.send(topic, jsonService.convertToJson(event));
    }

    public void publishShipmentDeleteEvent(String topic, ShipmentDeletedEvent event) {
        kafkaTemplate.send(topic, jsonService.convertToJson(event));
    }
}
