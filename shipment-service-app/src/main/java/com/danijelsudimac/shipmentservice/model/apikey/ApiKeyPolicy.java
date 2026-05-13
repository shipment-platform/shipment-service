package com.danijelsudimac.shipmentservice.model.apikey;

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
