package com.danijelsudimac.shipmentservice.model.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ShipmentUpdatedDto(
        @NotBlank String idempotencyKey,
        @NotBlank @Size(max = 50) String externalId,
        @NotBlank @Size(max = 50) String trackingNumber,
        @NotBlank @Size(max = 50) String orderId,
        @Nullable ShipmentStatus status,
        @Nullable Instant estimatedPickup,
        @Nullable Instant actualPickup,
        @Nullable Instant estimatedDelivery,
        @Nullable Instant actualDelivery
) {}
