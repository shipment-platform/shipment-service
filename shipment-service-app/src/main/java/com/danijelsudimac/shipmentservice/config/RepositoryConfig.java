package com.danijelsudimac.shipmentservice.config;

import com.danijelsudimac.shipmentservice.repository.ApiKeyPolicyRepository;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import com.danijelsudimac.shipmentservice.repository.mock.DummyApiKeyPolicyRepository;
import com.danijelsudimac.shipmentservice.repository.mock.DummyOutboxEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = "dev.mock.repositories.enabled",
        havingValue = "true"
)
public class RepositoryConfig {

    @Bean
    public ApiKeyPolicyRepository apiKeyPolicyRepository() {
        return new DummyApiKeyPolicyRepository();
    }

    @Bean
    public OutboxEventRepository outboxEventRepository() {
        return new DummyOutboxEventRepository();
    }
}
