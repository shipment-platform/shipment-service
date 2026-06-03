package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

class RedisRateLimitServiceTest {

    private StringRedisTemplate redisTemplate;
    private RedisRateLimitService service;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        service = new RedisRateLimitService(redisTemplate);
    }

    @Test
    void shouldAllowRequestWhenRedisReturns1() {
        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setClientId(1L);
        policy.setNumberOfRequestsPerDay(240L);

        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(1L);

        boolean result = service.allowRequest(policy);

        assertTrue(result);
    }

    @Test
    void shouldDenyRequestWhenRedisReturns0() {
        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setClientId(1L);
        policy.setNumberOfRequestsPerDay(240L);

        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
                .thenReturn(0L);

        boolean result = service.allowRequest(policy);

        assertFalse(result);
    }

    @Test
    void shouldBuildCorrectKey() {
        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setClientId(99L);
        policy.setNumberOfRequestsPerDay(240L);

        when(redisTemplate.execute(any(), anyList(), any(), any(), any()))
                .thenReturn(1L);

        service.allowRequest(policy);

        verify(redisTemplate).execute(
                any(),
                eq(List.of("rate_limit:99")),
                any(), any(), any()
        );
    }
}