package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.apikey.ApiKeyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyPolicyRepository extends JpaRepository<ApiKeyPolicy, Long> {
        Optional<ApiKeyPolicy> findByClientId(Long clientId);

        Optional<ApiKeyPolicy> findByApiKey(String apiKey);
}
