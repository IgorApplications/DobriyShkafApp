package com.iapp.iapp_messenger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Deprecated
public class RedisConfig {

    public RedisTemplate<String, String> redTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        template.setConnectionFactory(factory);

        // ключи
        template.setKeySerializer(new StringRedisSerializer());

        // значения
        template.setValueSerializer(new StringRedisSerializer());

        // для Set / Hash
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
