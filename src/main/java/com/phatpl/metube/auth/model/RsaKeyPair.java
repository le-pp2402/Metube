package com.phatpl.metube.auth.model;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Objects;

import com.phatpl.metube.common.message.ValidationMessage;

public record RsaKeyPair(
    String id,
    RSAPrivateKey privateKey,
    RSAPublicKey publicKey,
    Instant createdAt) {
  public RsaKeyPair {
    Objects.requireNonNull(id, ValidationMessage.notNull("id"));
    Objects.requireNonNull(privateKey, ValidationMessage.notNull("privateKey"));
    Objects.requireNonNull(publicKey, ValidationMessage.notNull("publicKey"));
    Objects.requireNonNull(createdAt, ValidationMessage.notNull("createdAt"));

    if (id.isBlank()) {
      throw new IllegalArgumentException(ValidationMessage.notBlank("id"));
    }
  }
}
