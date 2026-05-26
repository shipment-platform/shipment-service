package com.danijelsudimac.shipmentservice.service.mock;

import com.danijelsudimac.shipmentservice.service.IdempotencyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class DummyIdempotencyService implements IdempotencyService {
    @Override
    public boolean shouldDenyRequest(String idempotencyKey, Long clientId) {
        return false;
    }
}
