package com.danijelsudimac.shipmentservice.repository.mock;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import com.danijelsudimac.shipmentservice.repository.ApiKeyPolicyRepository;

import java.util.Optional;

public class DummyApiKeyPolicyRepository implements ApiKeyPolicyRepository {

    @Override
    public Optional<ApiKeyPolicy> findByClientId(Long clientId) {
        return Optional.empty();
    }

    @Override
    public Optional<ApiKeyPolicy> findByApiKey(String apiKey) {
        return Optional.empty();
    }

    @Override
    public ApiKeyPolicy save(ApiKeyPolicy apiKeyPolicy) {
        return apiKeyPolicy;
    }

    @Override
    public void delete(ApiKeyPolicy existing) {}
}
