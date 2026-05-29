package com.danijelsudimac.shipmentservice.model.dto;

import jakarta.validation.constraints.NotBlank;

public record Address (
        @NotBlank String addressLine,
        @NotBlank String city,
        @NotBlank String country,
        @NotBlank String postalCode,
        String state) { }
