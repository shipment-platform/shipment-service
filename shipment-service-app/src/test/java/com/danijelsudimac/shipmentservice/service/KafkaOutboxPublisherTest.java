package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType.CREATE_SHIPMENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaOutboxPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private ShipmentMetrics shipmentMetrics;

    @Spy
    private PayloadSerializator payloadSerializator = new AvroSerializator();
    @InjectMocks
    private KafkaOutboxPublisher kafkaOutboxPublisher;
    private OutboxEvent outboxEvent;
    @BeforeEach
    void setup() throws IOException {
        outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(CREATE_SHIPMENT);
        outboxEvent.setAggregateId("shipment-123");
        outboxEvent.setTopic(KafkaOutboxPublisher.SHIPMENT_INGEST_TOPIC);

        ShipmentCreatedEvent event =
                mock(ShipmentCreatedEvent.class);

        byte[] payload = payloadSerializator.serialize(event);

        outboxEvent.setPayload(payload);
    }

    @Test
    void shouldPublishEventSuccessfully() {

        when(repository.lockNextBatch(50))
                .thenReturn(List.of(outboxEvent));

        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(
                eq(KafkaOutboxPublisher.SHIPMENT_INGEST_TOPIC),
                eq("shipment-123"),
                any()
        )).thenReturn(future);

        kafkaOutboxPublisher.publish();

        verify(kafkaTemplate)
                .send(
                        eq(KafkaOutboxPublisher.SHIPMENT_INGEST_TOPIC),
                        eq("shipment-123"),
                        any()
                );

        verify(repository).save(outboxEvent);
        verify(shipmentMetrics).incrementPublished();

        assertTrue(outboxEvent.getPublished());
        assertNotNull(outboxEvent.getPublishedAt());
    }

    @Test
    void shouldNotSaveWhenKafkaPublishFails() {

        when(repository.lockNextBatch(50))
                .thenReturn(List.of(outboxEvent));

        CompletableFuture<SendResult<String, Object>> future =
                new CompletableFuture<>();

        future.completeExceptionally(
                new RuntimeException("Kafka failure"));

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(future);

        kafkaOutboxPublisher.publish();

        verify(repository, never())
                .save(any());

        verify(shipmentMetrics, never())
                .incrementPublished();

        assertTrue(outboxEvent.getPublished() == null || !outboxEvent.getPublished());
        assertNull(outboxEvent.getPublishedAt());
    }

    @Test
    void shouldDoNothingWhenNoEventsExist() {

        when(repository.lockNextBatch(50))
                .thenReturn(List.of());

        kafkaOutboxPublisher.publish();

        verifyNoInteractions(kafkaTemplate);

        verify(repository, never())
                .save(any());

        verifyNoInteractions(shipmentMetrics);
    }

    @Test
    void shouldContinueProcessingWhenDeserializationFails() {

        outboxEvent.setPayload(new byte[]{1,2,3});

        when(repository.lockNextBatch(50))
                .thenReturn(List.of(outboxEvent));

        kafkaOutboxPublisher.publish();

        verifyNoInteractions(kafkaTemplate);

        verify(repository, never())
                .save(any());

        verifyNoInteractions(shipmentMetrics);
    }
}