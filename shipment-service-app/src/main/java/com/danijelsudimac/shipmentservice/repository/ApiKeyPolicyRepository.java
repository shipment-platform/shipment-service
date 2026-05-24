package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;

import java.util.Optional;

public interface ApiKeyPolicyRepository {
        Optional<ApiKeyPolicy> findByClientId(Long clientId);

        Optional<ApiKeyPolicy> findByApiKey(String apiKey);

        ApiKeyPolicy save(ApiKeyPolicy apiKeyPolicy);

        void delete(ApiKeyPolicy existing);
}
