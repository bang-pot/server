package com.banglog.common.idempotency;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyKeyStore implements IdempotencyKeyStore {

	private static final String PROCESSING = "PROCESSING";

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public boolean reserve(String key, Duration ttl) {
		Boolean reserved = stringRedisTemplate.opsForValue().setIfAbsent(key, PROCESSING, ttl);
		return Boolean.TRUE.equals(reserved);
	}
}
