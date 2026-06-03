package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.common.Address;
import com.danijelsudimac.shipmentservice.model.common.Item;
import com.danijelsudimac.shipmentservice.model.common.ShippingMethod;
import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class KafkaOutboxPublisherIT {

    @Container
    private static final org.testcontainers.kafka.KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("apache/kafka-native:3.8.0"))
                    .waitingFor(Wait.forListeningPort());
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setup() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );
        consumer = new DefaultKafkaConsumerFactory<>( props, new StringDeserializer(),
                new JsonDeserializer<>(Object.class) ).createConsumer();
        consumer.subscribe( java.util.List.of( KafkaOutboxPublisher.SHIPMENT_INGEST_TOPIC ) );
    }

    @Autowired
    private KafkaOutboxPublisher kafkaOutboxPublisher;

    @Autowired
    private OutboxEventRepository repository;

    @Autowired
    private PayloadSerializator payloadSerializator;

    private Consumer<String, Object> consumer;

    @Test
    void shouldPublishOutboxEventToKafka() throws IOException {
        ShipmentCreatedEvent event =
                new ShipmentCreatedEvent(
                        "key-2",                 // idempotencyKey
                        "DHL",                      // carrier
                        1L,                        // clientId
                        null,              // createdAt
                        null, // estimatedPickup
                        null, // estimatedDelivery
                        Instant.now(),  // eventTimestamp
                        "EXT-10001",                // externalId
                        List.of(
                                new Item("ITEM-1", 2, "BOX",2d),
                                new Item("ITEM-2", 1, "BOX",2d)
                        ),
                        "ORD-445566",               // orderId
                        new Address(
                                "Industrial Zone 5",
                                "Belgrade",
                                "Serbia",
                                "11000",
                                ""
                        ),
                        "warehouse@example.com",   // originEmail
                        "Warehouse BG",            // originName
                        "+38160123456",            // originPhoneNumber
                        new Address(
                                "Main Street 10",
                                "Novi Sad",
                                "Serbia",
                                "21000",
                                ""
                        ),
                        "john.doe@example.com",    // recipientEmail
                        "John Doe",                // recipientName
                        "+38164111222",            // recipientPhoneNumber
                        ShippingMethod.EXPRESS,    // shippingMethod
                        null,                       // status (nullable)
                        "TRK-998877"               // trackingNumber
                );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("Shipment");
        outboxEvent.setAggregateId("shipment-123");
        outboxEvent.setTopic(KafkaOutboxPublisher.SHIPMENT_INGEST_TOPIC);
        outboxEvent.setEventType(OutboxEventType.CREATE_SHIPMENT);
        outboxEvent.setPayload(payloadSerializator.serialize(event));

        repository.save(outboxEvent);
        kafkaOutboxPublisher.publish();

        ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());
        ConsumerRecord<String, Object> record = records.iterator().next();
        assertEquals("shipment-123", record.key());

        var unpublished = repository.lockNextBatch(10);
        assertEquals(0,unpublished.size());
    }
}