package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.entity.OutboxEventStatus;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.repository.OutboxEventJPARepository;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

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
    private OutboxEventRepository jdbcRepository;

    @Mock
    private OutboxEventJPARepository repository;

    @Mock
    private ShipmentMetrics shipmentMetrics;

    @InjectMocks
    private KafkaOutboxPublisher kafkaOutboxPublisher;
    private OutboxEvent outboxEvent;

    @BeforeEach
    void setup() {
        outboxEvent = new OutboxEvent();
        outboxEvent.setEventType(CREATE_SHIPMENT);
        outboxEvent.setAggregateId("shipment-123");
        outboxEvent.setTopic("shipment-ingest");

        ShipmentCreatedEvent event = ShipmentCreatedEvent.getDefaultInstance();
        outboxEvent.setPayload(event.toByteArray());
    }

    @Test
    void shouldPublishEventSuccessfully() {

        when(jdbcRepository.lockNextBatch(50))
                .thenReturn(List.of(outboxEvent));

        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send(
                eq("shipment-ingest"),
                eq("shipment-123"),
                any()
        )).thenReturn(future);

        kafkaOutboxPublisher.publish();

        verify(kafkaTemplate)
                .send(
                        eq("shipment-ingest"),
                        eq("shipment-123"),
                        any()
                );

        verify(jdbcRepository).markPublished(any(),any());
        verify(shipmentMetrics).incrementPublished();
    }

    @Test
    void shouldNotSaveWhenKafkaPublishFails() {

        when(jdbcRepository.lockNextBatch(50))
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

        assertTrue(outboxEvent.getStatus() == null || outboxEvent.getStatus() != OutboxEventStatus.PUBLISHED);
        assertNull(outboxEvent.getPublishedAt());
    }

    @Test
    void shouldDoNothingWhenNoEventsExist() {

        when(jdbcRepository.lockNextBatch(50))
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

        when(jdbcRepository.lockNextBatch(50))
                .thenReturn(List.of(outboxEvent));

        kafkaOutboxPublisher.publish();

        verifyNoInteractions(kafkaTemplate);

        verify(repository, never())
                .save(any());

        verifyNoInteractions(shipmentMetrics);
    }
}