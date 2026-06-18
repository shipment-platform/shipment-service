package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.entity.ApiKeyPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
@ConditionalOnProperty(
        value = "dev.mock.service.idempotency.enabled",
        havingValue = "false"
)
public class RedisRateLimitService implements RateLimitService{

    private static final String TOKEN_BUCKET_SCRIPT = """
        local tokens = tonumber(redis.call("HGET", KEYS[1], "tokens") or ARGV[3])
        local last = tonumber(redis.call("HGET", KEYS[1], "last") or "0")
        
        local now = tonumber(ARGV[1])
        local rate = tonumber(ARGV[2])
        local capacity = tonumber(ARGV[3])
        
        local delta = math.max(0, now - last)
        local refill = delta * rate
        
        tokens = math.min(capacity, tokens + refill)
        
        if tokens < 1 then
          return 0
        else
          tokens = tokens - 1
          redis.call("HSET", KEYS[1], "tokens", tokens)
          redis.call("HSET", KEYS[1], "last", now)
          redis.call("EXPIRE", KEYS[1], 86400)
          return 1
        end
    """;
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> script;

    public RedisRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(TOKEN_BUCKET_SCRIPT);
        redisScript.setResultType(Long.class);
        this.script = redisScript;
    }

    public boolean allowRequest( ApiKeyPolicy apiKeyPolicy) {
        String key = "rate_limit:" + apiKeyPolicy.getClientId();
        long nowHours = Instant.now().getEpochSecond()/3600;
        Long result = redisTemplate.execute(
                script,
                List.of(key),         // KEYS
                String.valueOf(nowHours),  // ARGV[1]
                String.valueOf(apiKeyPolicy.getNumberOfRequestsPerDay()/24),                  // ARGV[2] = refill rate
                String.valueOf(apiKeyPolicy.getNumberOfRequestsPerDay())                  // ARGV[3] = bucket capacity
        );
        return result == 1;
    }
}
