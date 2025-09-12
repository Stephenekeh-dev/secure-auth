package com.steve.secure_auth.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveOtp(String key, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, key, ttlSeconds, TimeUnit.SECONDS);
    }

    public String getOtp(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}