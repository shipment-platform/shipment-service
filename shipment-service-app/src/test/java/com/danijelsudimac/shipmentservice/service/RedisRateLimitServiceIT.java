package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@Import({
        RedisAutoConfiguration.class,
        RedisRateLimitService.class
})
class RedisRateLimitServiceIT extends BaseRedisClass{
    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanRedis() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    void shouldAllowInitialRequests() {
        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setClientId(1L);
        policy.setNumberOfRequestsPerDay(24L); // 1 per hour

        boolean first = rateLimitService.allowRequest(policy);

        assertTrue(first);
    }

    @Test
    void shouldEventuallyDenyWhenExhausted() {
        ApiKeyPolicy policy = new ApiKeyPolicy();
        policy.setClientId(2L);
        policy.setNumberOfRequestsPerDay(2L); // very small bucket

        boolean first = rateLimitService.allowRequest(policy);
        boolean second = rateLimitService.allowRequest(policy);
        boolean third = rateLimitService.allowRequest(policy);

        assertTrue(first);
        assertTrue(second);
        assertFalse(third);
    }

    @Test
    void differentClientsHaveSeparateBuckets() {
        ApiKeyPolicy p1 = new ApiKeyPolicy();
        p1.setClientId(1L);
        p1.setNumberOfRequestsPerDay(1L);

        ApiKeyPolicy p2 = new ApiKeyPolicy();
        p2.setClientId(2L);
        p2.setNumberOfRequestsPerDay(1L);

        assertTrue(rateLimitService.allowRequest(p1));
        assertTrue(rateLimitService.allowRequest(p2));
    }
}