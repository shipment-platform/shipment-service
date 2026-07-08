package com.danijelsudimac.shipmentservice.model.entity;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}
