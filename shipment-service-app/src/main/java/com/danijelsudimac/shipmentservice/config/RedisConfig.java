package com.danijelsudimac.shipmentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

//@Configuration
//@Profile("!dev")
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${SPRING_DATA_REDIS_HOST}") String host,
            @Value("${SPRING_DATA_REDIS_PORT}") int port,
            @Value("${SPRING_DATA_REDIS_USERNAME}") String username,
            @Value("${SPRING_DATA_REDIS_PASSWORD}") String password) {

        RedisStandaloneConfiguration redisConfig =
                new RedisStandaloneConfiguration(host, port);

        redisConfig.setUsername(username);
        redisConfig.setPassword(password);

        LettuceClientConfiguration clientConfig =
                LettuceClientConfiguration.builder()
                        .useSsl()
                        .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }
    @Bean
    public StringRedisTemplate redisTemplate(
            LettuceConnectionFactory factory
    ) {
        return new StringRedisTemplate(factory);
    }
}
