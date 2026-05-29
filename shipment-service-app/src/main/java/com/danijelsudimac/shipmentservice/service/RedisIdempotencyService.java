package com.danijelsudimac.shipmentservice.service;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("prod")
public class RedisIdempotencyService implements IdempotencyService {

    private static final String IDEMPOTENCY_SCRIPT = """
        local result = redis.call(
            "SET",
            KEYS[1],
            "1",
            "EX",
            86400,
            "NX"
        )
        
        if result then
            return 1
        else
            return 0
        end
    """;
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> script;

    public RedisIdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(IDEMPOTENCY_SCRIPT);
        redisScript.setResultType(Long.class);
        this.script = redisScript;
    }

    public boolean shouldDenyRequest(String idempotencyKey, Long clientId) {
        String key = "idempotency:" + idempotencyKey + ":" + clientId;
        Long result = redisTemplate.execute(script, List.of(key));
        return result == 0;
    }
}
