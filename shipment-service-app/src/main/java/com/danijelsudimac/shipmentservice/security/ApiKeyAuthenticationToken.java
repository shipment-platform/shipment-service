package com.danijelsudimac.shipmentservice.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String apiKey;

    @Getter
    private final Long clientId;

    public ApiKeyAuthenticationToken(String apiKey, Long clientId) {
        super(null);
        this.apiKey = apiKey;
        this.clientId = clientId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return clientId;
    }
}
