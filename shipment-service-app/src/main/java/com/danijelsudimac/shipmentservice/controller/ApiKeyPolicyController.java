package com.danijelsudimac.shipmentservice.controller;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import com.danijelsudimac.shipmentservice.service.ApiKeyConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/apikey-policy")
@RequiredArgsConstructor
public class ApiKeyPolicyController {

    private final ApiKeyConfigurationService apiKeyConfigurationService;

    @GetMapping("/{clientId}")
    public ResponseEntity<ApiKeyPolicy> getApiKeyPolicyByClientId(@PathVariable Long clientId) {
        Optional<ApiKeyPolicy> apiKeyPolicy = apiKeyConfigurationService.getApiKeyPolicyByClientId(clientId);
        return apiKeyPolicy.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiKeyPolicy> createApiKeyPolicy(@RequestBody ApiKeyPolicy apiKeyPolicy) {
        ApiKeyPolicy createdPolicy = apiKeyConfigurationService.createApiKeyPolicy(apiKeyPolicy);
        return ResponseEntity.ok(createdPolicy);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiKeyPolicy> updateApiKeyPolicy(@PathVariable Long id, @RequestBody ApiKeyPolicy apiKeyPolicy) {
        Optional<ApiKeyPolicy> updatedPolicy = apiKeyConfigurationService.updateApiKeyPolicy(id, apiKeyPolicy);
        return updatedPolicy.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiKeyPolicy(@PathVariable Long id) {
        boolean deleted = apiKeyConfigurationService.deleteApiKeyPolicy(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}