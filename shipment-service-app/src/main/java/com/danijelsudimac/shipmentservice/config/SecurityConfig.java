package com.danijelsudimac.shipmentservice.config;

import com.danijelsudimac.shipmentservice.security.ApiKeyAuthFilter;
import com.danijelsudimac.shipmentservice.service.ApiKeyConfigurationService;
import com.danijelsudimac.shipmentservice.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final RateLimitService rateLimitService;
    private final ApiKeyConfigurationService apiKeyConfigurationService;

    @Bean
    @Order(1)
    SecurityFilterChain actuatorAndApiKeyFilterChain(HttpSecurity http)
            throws Exception {

        http.securityMatcher(
                        "/actuator/**",
                        "/api/apikey-policy")
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new ApiKeyAuthFilter(rateLimitService, apiKeyConfigurationService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}