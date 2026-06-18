package com.danijelsudimac.shipmentservice.service.mock;

import com.danijelsudimac.shipmentservice.service.IdempotencyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        value = "dev.mock.service.idempotency.enabled",
        havingValue = "true"
)
public class DummyIdempotencyService implements IdempotencyService {
    @Override
    public boolean shouldDenyRequest(String idempotencyKey, Long clientId) {
        return false;
    }
}
