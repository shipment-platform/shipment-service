package com.danijelsudimac.shipmentservice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "api_key_policy")
public class ApiKeyPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String apiKey;

    @NotNull
    @Column(unique = true)
    private Long clientId;

    @NotNull
    private Long numberOfRequestsPerDay;

    @NotNull
    private Boolean active;
}
