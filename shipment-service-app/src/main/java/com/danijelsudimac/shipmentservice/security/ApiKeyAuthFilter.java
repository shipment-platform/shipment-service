package com.danijelsudimac.shipmentservice.security;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import com.danijelsudimac.shipmentservice.service.ApiKeyConfigurationService;
import com.danijelsudimac.shipmentservice.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;


@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String RATE_LIMIT_EXCEEDED_MESSAGE = "Rate limit exceeded";
    private static final String INACTIVE_API_KEY_MESSAGE = "API Key is inactive";
    private static final String INVALID_API_KEY_MESSAGE = "Invalid API Key";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

    private final RateLimitService rateLimitService;
    private final ApiKeyConfigurationService apiKeyConfigurationService;

    public ApiKeyAuthFilter(RateLimitService rateLimitService, ApiKeyConfigurationService apiKeyConfigurationService) {
        this.rateLimitService = rateLimitService;
        this.apiKeyConfigurationService = apiKeyConfigurationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            String apiKey = request.getHeader(API_KEY_HEADER);
            Optional<ApiKeyPolicy> apiKeyPolicy = apiKeyConfigurationService.getApiKeyPolicyByApiKey(apiKey);
            if (apiKeyPolicy.isPresent()) {
                ApiKeyPolicy policy = apiKeyPolicy.get();
                if (policy.getActive()) {
                    // check rate limit using rateLimitService, if exceeded return 429
                    if (rateLimitService.allowRequest(policy)) {

                        var authentication = new ApiKeyAuthenticationToken(apiKey, policy.getClientId());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        filterChain.doFilter(request, response); // Proceed if API key is valid
                        return;
                    } else {
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        response.getWriter().write(RATE_LIMIT_EXCEEDED_MESSAGE);
                        return;
                    }
                } else {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write(INACTIVE_API_KEY_MESSAGE);
                    return;
                }
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(INVALID_API_KEY_MESSAGE);
        } catch (Exception e) {
            logger.error(e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write(INTERNAL_SERVER_ERROR_MESSAGE);
        }
    }
}