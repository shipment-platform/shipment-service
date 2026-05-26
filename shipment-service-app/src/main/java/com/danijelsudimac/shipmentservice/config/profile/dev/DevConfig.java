package com.danijelsudimac.shipmentservice.config.profile.dev;

import com.danijelsudimac.shipmentservice.repository.ApiKeyPolicyRepository;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import com.danijelsudimac.shipmentservice.repository.mock.DummyApiKeyPolicyRepository;
import com.danijelsudimac.shipmentservice.repository.mock.DummyOutboxEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevConfig {

    @Bean
    public ApiKeyPolicyRepository apiKeyPolicyRepository() {
        return new DummyApiKeyPolicyRepository();
    }

    @Bean
    public OutboxEventRepository outboxEventRepository() {
        return new DummyOutboxEventRepository();
    }
}
