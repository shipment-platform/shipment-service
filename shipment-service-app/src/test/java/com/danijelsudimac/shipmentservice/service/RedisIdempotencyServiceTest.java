package com.danijelsudimac.shipmentservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisIdempotencyServiceTest {

    private StringRedisTemplate redisTemplate;
    private RedisIdempotencyService service;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        service = new RedisIdempotencyService(redisTemplate);
    }

    @Test
    void shouldAllowRequestWhenRedisReturns1() {
        when(redisTemplate.execute(any(RedisScript.class), anyList()))
                .thenReturn(1L);

        boolean result = service.shouldDenyRequest("key1", 1L);

        assertFalse(result);
    }

    @Test
    void shouldDenyRequestWhenRedisReturns0() {
        when(redisTemplate.execute(any(RedisScript.class), anyList()))
                .thenReturn(0L);

        boolean result = service.shouldDenyRequest("key1", 1L);

        assertTrue(result);
    }

    @Test
    void shouldBuildCorrectRedisKey() {
        when(redisTemplate.execute(any(RedisScript.class), anyList()))
                .thenReturn(1L);

        service.shouldDenyRequest("abc", 99L);

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);

        verify(redisTemplate).execute(any(RedisScript.class), captor.capture());

        assertTrue(captor.getValue().get(0)
                .contains("idempotency:abc:99"));
    }
}
