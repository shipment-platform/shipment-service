package com.danijelsudimac.shipmentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@SpringBootTest
@Testcontainers
public class ShipmentServiceApplicationIT {

    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Container
    private static KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
                            .asCompatibleSubstituteFor("apache/kafka"))
                    .waitingFor(Wait.forListeningPort());

    @Container
    private static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add(
                "spring.data.redis.port",
                () -> redis.getMappedPort(6379)
        );
    }

    @Test
    void shouldStartContext() {
    }
}
