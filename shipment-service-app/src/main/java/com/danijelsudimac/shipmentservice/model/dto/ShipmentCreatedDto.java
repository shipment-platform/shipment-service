package com.danijelsudimac.shipmentservice.model.dto;

import com.danijelsudimac.shipmentservice.model.common.Address;
import com.danijelsudimac.shipmentservice.model.common.Goods;
import com.danijelsudimac.shipmentservice.model.common.ShipmentStatus;
import com.danijelsudimac.shipmentservice.model.common.ShippingMethod;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record ShipmentCreatedDto(
        @NotBlank String idempotencyKey,
        @NotBlank @Size(max = 50) String externalId,
        @NotBlank @Size(max = 50) String trackingNumber,
        @NotBlank @Size(max = 50) String orderId,
        @Nullable ShipmentStatus status,
        @NotBlank @Size(max = 50) String carrier,
        @NotNull ShippingMethod shippingMethod,
        @NotBlank @Size(max = 100) String recipientName,
        @NotNull Address recipientAddress,
        @NotBlank @Pattern(regexp = "\\+?[0-9\\-]+") String recipientPhoneNumber,
        @Email @NotBlank String recipientEmail,
        @NotBlank @Size(max = 100) String originName,
        @NotNull Address originAddress,
        @NotBlank @Pattern(regexp = "\\+?[0-9\\-]+") String originPhoneNumber,
        @Email @NotBlank String originEmail,
        @NotNull List<Goods> goods,
        @Nullable Instant estimatedPickup,
        @Nullable Instant estimatedDelivery,
        @Nullable Instant createdAt
) {}