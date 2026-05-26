package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;

public interface RateLimitService {
    boolean allowRequest( ApiKeyPolicy apiKeyPolicy);
}
