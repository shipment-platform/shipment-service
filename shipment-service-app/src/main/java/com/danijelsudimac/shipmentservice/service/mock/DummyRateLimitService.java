package com.danijelsudimac.shipmentservice.service.mock;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import com.danijelsudimac.shipmentservice.service.RateLimitService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod")
public class DummyRateLimitService implements RateLimitService {
    @Override
    public boolean allowRequest(ApiKeyPolicy apiKeyPolicy) {
        return true;
    }
}
