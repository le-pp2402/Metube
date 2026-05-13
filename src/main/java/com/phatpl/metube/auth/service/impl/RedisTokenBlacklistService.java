package com.phatpl.metube.auth.service.impl;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.phatpl.metube.auth.service.TokenBlacklistService;

@Service
public class RedisTokenBlacklistService implements TokenBlacklistService {
  private static final String BLACKLIST_KEY_PREFIX = "auth:blacklist:jti:";

  private final StringRedisTemplate redisTemplate;

  public RedisTokenBlacklistService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void add(Long jti, Instant expiry) {
    if (jti == null || expiry == null) {
      return;
    }

    String key = getKey(jti);

    long ttl = Duration.between(Instant.now(), expiry).getSeconds();

    if (ttl > 0) {
      redisTemplate.opsForValue().set(key, "revoked", ttl);
    }
  }

  @Override
  public boolean isBlacklisted(Long jti) {
    if (jti == null || jti <= 0) {
      return false;
    }

    String key = getKey(jti);

    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  private String getKey(Long jti) {
    return BLACKLIST_KEY_PREFIX + jti;
  }
}
