package com.phatpl.metube.auth.service.impl;

import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWKSet;
import com.phatpl.metube.auth.model.RsaKeyPair;
import com.phatpl.metube.auth.service.KeyProvider;
import com.phatpl.metube.common.id.HutoolSnowflakeIdGenerator;
import com.phatpl.metube.common.id.IdGenerator;
import com.phatpl.metube.common.id.SnowflakeId;

import cn.hutool.core.lang.generator.SnowflakeGenerator;

@Component
public class RedisKeyProvider implements KeyProvider {

  private static final String KEY_PREFIX = "auth:keys:";
  private static final String CURRENT_KEY = "auth:keys:current";

  private StringRedisTemplate redisTemplate;
  private static IdGenerator idGenerator;

  public RedisKeyProvider(StringRedisTemplate redisTemplate, IdGenerator idGenerator) {
    this.redisTemplate = redisTemplate;
    RedisKeyProvider.idGenerator = idGenerator;
  }

  @Override
  public RsaKeyPair getCurrent() {
    var keyId = redisTemplate.opsForValue().get(CURRENT_KEY);

    if (keyId == null) {
      var newKey = rotate();
      redisTemplate.opsForValue().set(CURRENT_KEY, genKeyId());
      return newKey;
    }
  }

  @Override
  public Optional<RsaKeyPair> getById(Long kid) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getById'");
  }

  @Override
  public RsaKeyPair rotate() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'rotate'");
  }

  @Override
  public JWKSet jwkSet() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'jwkSet'");
  }

  private String genKeyId() {
    return "kid_" + String.valueOf(idGenerator.nextId());
  }
}
