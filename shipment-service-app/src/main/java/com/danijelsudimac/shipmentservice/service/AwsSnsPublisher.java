package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType.CREATE_SHIPMENT;

@Service
@Slf4j
@ConditionalOnProperty(
        value = "aws.sns.enabled",
        havingValue = "true"
)
public class AwsSnsPublisher {

    private static final String PUBLISHING_ERROR_MESSAGE = "SNS publish failed";
    private static final String MESSAGE_TYPE_HEADER = "message-type";
    private final SnsClient snsClient;
    private final OutboxEventRepository repository;
    private final ShipmentMetrics shipmentMetrics;
    private final String topicArn;

    public AwsSnsPublisher(SnsClient snsClient, OutboxEventRepository repository, ShipmentMetrics shipmentMetrics,
                           @Value("${aws.sns.shipment-topic-arn}") String topicArn) {
        this.snsClient = snsClient;
        this.repository = repository;
        this.shipmentMetrics = shipmentMetrics;
        this.topicArn = topicArn;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publish() {
        var events = repository.lockNextBatch(50);
        for (OutboxEvent event : events) {
            try {
                switch (event.getEventType()) {
                    case CREATE_SHIPMENT -> {
                        var unpackedEvent = ShipmentCreatedEvent.parseFrom(event.getPayload());
                        sendMessage(unpackedEvent, CREATE_SHIPMENT.name(), unpackedEvent.getExternalId());
                    }
                    case UPDATE_SHIPMENT -> {
                        var unpackedEvent = ShipmentUpdatedEvent.parseFrom(event.getPayload());
                        sendMessage(unpackedEvent, CREATE_SHIPMENT.name(), unpackedEvent.getExternalId());
                    }
                    case DELETE_SHIPMENT -> {
                        var unpackedEvent = ShipmentDeletedEvent.parseFrom(event.getPayload());
                        sendMessage(unpackedEvent, CREATE_SHIPMENT.name(), unpackedEvent.getExternalId());
                    }
                }
                event.setPublished(true);
                event.setPublishedAt(Instant.now());
                repository.save(event);
                shipmentMetrics.incrementPublished();
            } catch (Exception ex) {
                shipmentMetrics.incrementFailed();
                log.error(PUBLISHING_ERROR_MESSAGE, ex);
            }
        }
    }

    private void sendMessage(GeneratedMessage payload, String messageType, String externalId)
            throws InvalidProtocolBufferException {
        Map<String, MessageAttributeValue> attributes = new HashMap<>();
        attributes.put(MESSAGE_TYPE_HEADER, MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(messageType)
                .build());

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .messageGroupId(externalId)
                .message(JsonFormat.printer().print(payload))
                .messageAttributes(attributes)
                .build();

        snsClient.publish(request);
    }
}
