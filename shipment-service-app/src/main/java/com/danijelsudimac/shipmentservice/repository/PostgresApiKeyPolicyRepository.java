package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PostgresApiKeyPolicyRepository extends JpaRepository<ApiKeyPolicy, Long>, ApiKeyPolicyRepository {
    Optional<ApiKeyPolicy> findByClientId(Long clientId);

    Optional<ApiKeyPolicy> findByApiKey(String apiKey);
}