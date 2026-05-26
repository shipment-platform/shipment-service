package com.danijelsudimac.shipmentservice.service;

public interface IdempotencyService {
    boolean shouldDenyRequest(String idempotencyKey, Long clientId);
}
