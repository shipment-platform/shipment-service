package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import com.danijelsudimac.shipmentservice.repository.ApiKeyPolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyConfigurationServiceTest {

    @Mock
    private ApiKeyPolicyRepository apiKeyPolicyRepository;

    @InjectMocks
    private ApiKeyConfigurationService apiKeyConfigurationService;

    @Test
    void shouldGetApiKeyPolicyByClientId() {
        Long clientId = 1L;

        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setClientId(clientId);

        when(apiKeyPolicyRepository.findByClientId(clientId))
                .thenReturn(Optional.of(policy));

        Optional<ApiKeyPolicy> result =
                apiKeyConfigurationService.getApiKeyPolicyByClientId(clientId);

        assertTrue(result.isPresent());
        assertEquals(clientId, result.get().getClientId());

        verify(apiKeyPolicyRepository)
                .findByClientId(clientId);
    }

    @Test
    void shouldReturnEmptyWhenPolicyByClientIdNotFound() {
        Long clientId = 1L;

        when(apiKeyPolicyRepository.findByClientId(clientId))
                .thenReturn(Optional.empty());

        Optional<ApiKeyPolicy> result =
                apiKeyConfigurationService.getApiKeyPolicyByClientId(clientId);

        assertTrue(result.isEmpty());

        verify(apiKeyPolicyRepository)
                .findByClientId(clientId);
    }

    @Test
    void shouldCreateApiKeyPolicy() {
        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setClientId(1L);

        when(apiKeyPolicyRepository.save(policy))
                .thenReturn(policy);

        ApiKeyPolicy result =
                apiKeyConfigurationService.createApiKeyPolicy(policy);

        assertNotNull(result);
        assertEquals(1L, result.getClientId());

        verify(apiKeyPolicyRepository)
                .save(policy);
    }

    @Test
    void shouldUpdateApiKeyPolicy() {
        Long clientId = 1L;

        ApiKeyPolicy existing = new ApiKeyPolicy();
        existing.setClientId(clientId);
        existing.setApiKey("old-key");
        existing.setActive(true);
        existing.setNumberOfRequestsPerDay(100L);

        ApiKeyPolicy update = new ApiKeyPolicy();
        update.setApiKey("new-key");
        update.setActive(false);
        update.setNumberOfRequestsPerDay(500L);

        ApiKeyPolicy saved = new ApiKeyPolicy();
        saved.setClientId(clientId);
        saved.setApiKey("new-key");
        saved.setActive(false);
        saved.setNumberOfRequestsPerDay(500L);

        when(apiKeyPolicyRepository.findByClientId(clientId))
                .thenReturn(Optional.of(existing));

        when(apiKeyPolicyRepository.save(any(ApiKeyPolicy.class)))
                .thenReturn(saved);

        Optional<ApiKeyPolicy> result =
                apiKeyConfigurationService.updateApiKeyPolicy(clientId, update);

        assertTrue(result.isPresent());

        ApiKeyPolicy updated = result.get();

        assertEquals("new-key", updated.getApiKey());
        assertFalse(updated.getActive());
        assertEquals(500, updated.getNumberOfRequestsPerDay());

        verify(apiKeyPolicyRepository)
                .findByClientId(clientId);

        verify(apiKeyPolicyRepository)
                .save(existing);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingMissingPolicy() {
        Long clientId = 1L;

        ApiKeyPolicy update = new ApiKeyPolicy();

        when(apiKeyPolicyRepository.findByClientId(clientId))
                .thenReturn(Optional.empty());

        Optional<ApiKeyPolicy> result =
                apiKeyConfigurationService.updateApiKeyPolicy(clientId, update);

        assertTrue(result.isEmpty());

        verify(apiKeyPolicyRepository)
                .findByClientId(clientId);

        verify(apiKeyPolicyRepository, never())
                .save(any());
    }

    @Test
    void shouldDeleteApiKeyPolicy() {
        Long clientId = 1L;

        ApiKeyPolicy existing = new ApiKeyPolicy();
        existing.setClientId(clientId);

        when(apiKeyPolicyRepository.findByClientId(clientId))
                .thenReturn(Optional.of(existing));

        boolean deleted =
                apiKeyConfigurationService.deleteApiKeyPolicy(clientId);

        assertTrue(deleted);

        verify(apiKeyPolicyRepository)
                .delete(existing);
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingPolicy() {
        Long clientId = 1L;

        when(apiKeyPolicyRepository.findByClientId(clientId))
                .thenReturn(Optional.empty());

        boolean deleted =
                apiKeyConfigurationService.deleteApiKeyPolicy(clientId);

        assertFalse(deleted);

        verify(apiKeyPolicyRepository, never())
                .delete(any());
    }

    @Test
    void shouldGetApiKeyPolicyByApiKey() {
        String apiKey = "test-api-key";

        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setApiKey(apiKey);

        when(apiKeyPolicyRepository.findByApiKey(apiKey))
                .thenReturn(Optional.of(policy));

        Optional<ApiKeyPolicy> result =
                apiKeyConfigurationService.getApiKeyPolicyByApiKey(apiKey);

        assertTrue(result.isPresent());
        assertEquals(apiKey, result.get().getApiKey());

        verify(apiKeyPolicyRepository)
                .findByApiKey(apiKey);
    }

    @Test
    void shouldReturnEmptyWhenApiKeyNotFound() {
        String apiKey = "missing-key";

        when(apiKeyPolicyRepository.findByApiKey(apiKey))
                .thenReturn(Optional.empty());

        Optional<ApiKeyPolicy> result =
                apiKeyConfigurationService.getApiKeyPolicyByApiKey(apiKey);

        assertTrue(result.isEmpty());

        verify(apiKeyPolicyRepository)
                .findByApiKey(apiKey);
    }
}

