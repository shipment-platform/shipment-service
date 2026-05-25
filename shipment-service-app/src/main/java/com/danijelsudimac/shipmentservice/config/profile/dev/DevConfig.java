package com.danijelsudimac.shipmentservice.config.profile.dev;

import com.danijelsudimac.shipmentservice.repository.ApiKeyPolicyRepository;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import com.danijelsudimac.shipmentservice.repository.mock.DummyApiKeyPolicyRepository;
import com.danijelsudimac.shipmentservice.repository.mock.DummyOutboxEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Configuration
@Profile("dev")
public class DevConfig {

    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public StringRedisTemplate stringRedisTemplate() {
        return new StringRedisTemplate() {
            @Override
            public <T> T execute(org.springframework.data.redis.core.script.RedisScript<T> script, java.util.List<String> keys, Object... args) {
                return null;
            }
        };
    }

    @Bean
    public ApiKeyPolicyRepository apiKeyPolicyRepository() {
        return new DummyApiKeyPolicyRepository();
    }

    @Bean
    public OutboxEventRepository outboxEventRepository() {
        return new DummyOutboxEventRepository();
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(new HashMap<>())) {

            @Override
            public CompletableFuture<SendResult<String, Object>> send(
                    String topic, Object data) {

                System.out.println("Fake send to " + topic + ": " + data);

                return CompletableFuture.completedFuture(null);
            }
        };
    }
}
