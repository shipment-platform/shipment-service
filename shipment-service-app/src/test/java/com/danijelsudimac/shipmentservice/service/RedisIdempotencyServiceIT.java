package com.danijelsudimac.shipmentservice.service;

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
        RedisIdempotencyService.class,
})
class RedisIdempotencyServiceIT extends BaseRedisClass{

    @Autowired
    private IdempotencyService service;

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
    void shouldAllowFirstRequest() {
        boolean result = service.shouldDenyRequest("k1", 1L);
        assertFalse(result);
    }

    @Test
    void shouldDenySecondRequest() {
        service.shouldDenyRequest("k2", 1L);
        boolean second = service.shouldDenyRequest("k2", 1L);
        assertTrue(second);
    }

    @Test
    void differentClientShouldNotConflict() {
        service.shouldDenyRequest("k3", 1L);
        boolean otherClient =
                service.shouldDenyRequest("k3", 2L);
        assertFalse(otherClient);
    }
}
