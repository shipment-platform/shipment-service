package com.danijelsudimac.shipmentservice.config;

import com.danijelsudimac.shipmentservice.security.ApiKeyAuthFilter;
import com.danijelsudimac.shipmentservice.service.ApiKeyConfigurationService;
import com.danijelsudimac.shipmentservice.service.RateLimitService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final RateLimitService rateLimitService;
    private final ApiKeyConfigurationService apiKeyConfigurationService;

    public SecurityConfig(RateLimitService rateLimitService, ApiKeyConfigurationService apiKeyConfigurationService) {
        this.rateLimitService = rateLimitService;
        this.apiKeyConfigurationService = apiKeyConfigurationService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/apikey-policy").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new ApiKeyAuthFilter(rateLimitService, apiKeyConfigurationService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}