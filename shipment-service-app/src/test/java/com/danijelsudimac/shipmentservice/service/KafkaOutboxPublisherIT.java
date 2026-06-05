package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.common.Address;
import com.danijelsudimac.shipmentservice.model.common.Item;
import com.danijelsudimac.shipmentservice.model.common.ShippingMethod;
import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
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
import org.springframework.kafka.core.KafkaTemplate;
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
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer");
        registry.add("spring.kafka.properties.schema.registry.url", () -> "mock://test");
        registry.add("schema.registry.url", () -> "mock://test");
    }

    @Autowired
    private KafkaTemplate<String, ShipmentCreatedEvent> kafkaTemplate;

    @BeforeEach
    void setup() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                "spring.kafka.properties.schema.registry.url", "mock://test",
                "schema.registry.url",  "mock://test"
        );
        consumer = new DefaultKafkaConsumerFactory<>( props, new StringDeserializer(),
                new KafkaProtobufDeserializer()).createConsumer();
        consumer.subscribe( java.util.List.of( "shipment-ingest" ));
    }

    @Autowired
    private KafkaOutboxPublisher kafkaOutboxPublisher;

    @Autowired
    private OutboxEventRepository repository;

    private Consumer<String, ShipmentCreatedEvent> consumer;

    @Test
    void shouldPublishOutboxEventToKafka() throws IOException {
        ShipmentCreatedEvent event =
                ShipmentCreatedEvent.newBuilder()
                        .setIdempotencyKey("key-2")
                        .setCarrier("DHL")
                        .setClientId(1L)

                        .setEventTimestamp(Instant.now().toEpochMilli())
                        .setExternalId("EXT-10001")
                        .setOrderId("ORD-445566")

                        .addAllItems(List.of(
                                Item.newBuilder()
                                        .setName("ITEM-1")
                                        .setQuantity(2)
                                        .setUnit("BOX")
                                        .setWeight(2d)
                                        .build(),

                                Item.newBuilder()
                                        .setName("ITEM-2")
                                        .setQuantity(1)
                                        .setUnit("BOX")
                                        .setWeight(2d)
                                        .build()
                        ))

                        .setOriginAddress(
                                Address.newBuilder()
                                        .setAddressLine("Industrial Zone 5")
                                        .setCity("Belgrade")
                                        .setCountry("Serbia")
                                        .setPostalCode("11000")
                                        .setState("")
                                        .build()
                        )
                        .setOriginEmail("warehouse@example.com")
                        .setOriginName("Warehouse BG")
                        .setOriginPhoneNumber("+38160123456")

                        .setRecipientAddress(
                                Address.newBuilder()
                                        .setAddressLine("Main Street 10")
                                        .setCity("Novi Sad")
                                        .setCountry("Serbia")
                                        .setPostalCode("21000")
                                        .setState("")
                                        .build()
                        )
                        .setRecipientEmail("john.doe@example.com")
                        .setRecipientName("John Doe")
                        .setRecipientPhoneNumber("+38164111222")

                        .setShippingMethod(ShippingMethod.SHIPPING_METHOD_EXPRESS)
                        .setTrackingNumber("TRK-998877")

                        .build();

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("Shipment");
        outboxEvent.setAggregateId("shipment-123");
        outboxEvent.setTopic("shipment-ingest");
        outboxEvent.setEventType(OutboxEventType.CREATE_SHIPMENT);
        outboxEvent.setPayload(event.toByteArray());

        repository.save(outboxEvent);
        kafkaOutboxPublisher.publish();

        ConsumerRecords<String, ShipmentCreatedEvent> records = consumer.poll(Duration.ofSeconds(10));
        assertFalse(records.isEmpty());
        ConsumerRecord<String, ShipmentCreatedEvent> record = records.iterator().next();
        assertEquals("shipment-123", record.key());

        var unpublished = repository.lockNextBatch(10);
        assertEquals(0,unpublished.size());
    }
}