package com.phatpl.metube.auth.model;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Objects;

import com.phatpl.metube.common.message.ValidationMessage;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

public record RsaKeyPair(
    Long kid,
    RSAPrivateKey privateKey,
    RSAPublicKey publicKey,
    Instant createdAt) {

  public RsaKeyPair(Long kid, RSAPrivateKey privateKey, RSAPublicKey publicKey) {
    this(kid, privateKey, publicKey, extractCreatedAt(kid));
  }

  // Snowflake and Instant have different epoch
  // Handle the conversion by adding the delta between the two epochs
  private static Instant extractCreatedAt(Long kid) {
    long timestamp = IdUtil.getSnowflake().getGenerateDateTime(kid);
    long delta = Snowflake.DEFAULT_TWEPOCH - Instant.EPOCH.toEpochMilli();
    return Instant.ofEpochMilli(timestamp + delta);
  }

  public RsaKeyPair {
    Objects.requireNonNull(kid, ValidationMessage.notNull("kid"));
    Objects.requireNonNull(privateKey, ValidationMessage.notNull("privateKey"));
    Objects.requireNonNull(publicKey, ValidationMessage.notNull("publicKey"));
    Objects.requireNonNull(createdAt, ValidationMessage.notNull("createdAt"));
    if (kid <= 0) {
      throw new IllegalArgumentException(ValidationMessage.notBlank("kid"));
    }
  }
}
