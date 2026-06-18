package com.danijelsudimac.shipmentservice.service.mock;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import com.danijelsudimac.shipmentservice.service.RateLimitService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        value = "dev.mock.service.rate-limit.enabled",
        havingValue = "true"
)
public class DummyRateLimitService implements RateLimitService {
    @Override
    public boolean allowRequest(ApiKeyPolicy apiKeyPolicy) {
        return true;
    }
}
