package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import com.danijelsudimac.shipmentservice.repository.ApiKeyPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiKeyConfigurationService {

    private final ApiKeyPolicyRepository apiKeyPolicyRepository;

    @Cacheable(value = "apiKeyPolicies", key = "#clientId")
    public Optional<ApiKeyPolicy> getApiKeyPolicyByClientId(Long clientId) {
        return apiKeyPolicyRepository.findByClientId(clientId);
    }

    public ApiKeyPolicy createApiKeyPolicy(ApiKeyPolicy apiKeyPolicy) {
        return apiKeyPolicyRepository.save(apiKeyPolicy);
    }

    @CacheEvict(value = "apiKeyPolicie", key = "#clientId")
    public Optional<ApiKeyPolicy> updateApiKeyPolicy(Long clientId, ApiKeyPolicy apiKeyPolicy) {
        return apiKeyPolicyRepository.findByClientId(clientId).map(existing -> {
            existing.setActive(apiKeyPolicy.getActive());
            existing.setApiKey(apiKeyPolicy.getApiKey());
            existing.setNumberOfRequestsPerDay(apiKeyPolicy.getNumberOfRequestsPerDay());
            return apiKeyPolicyRepository.save(existing);
        });
    }

    @CacheEvict(value = "apiKeyPolicie", key = "#clientId")
    public boolean deleteApiKeyPolicy(Long clientId) {
        return apiKeyPolicyRepository.findByClientId(clientId).map(existing -> {
            apiKeyPolicyRepository.delete(existing);
            return true;
        }).orElse(false);
    }

    public Optional<ApiKeyPolicy> getApiKeyPolicyByApiKey(String apiKey) {
        return apiKeyPolicyRepository.findByApiKey(apiKey);
    }
}
