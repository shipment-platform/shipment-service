package com.danijelsudimac.shipmentservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Item (
        @NotBlank String name,
        @NotNull Integer quantity,
        String unit,
        Double weight){}
