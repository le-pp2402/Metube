package com.phatpl.metube.auth.service.impl;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.phatpl.metube.auth.model.RsaKeyPair;
import com.phatpl.metube.auth.service.KeyProvider;
import com.phatpl.metube.common.id.IdGenerator;

import cn.hutool.core.codec.Base64;

@Component
public class RedisKeyProvider implements KeyProvider {
  private static final Logger logger = LoggerFactory.getLogger(RedisKeyProvider.class);

  private static final String RSA = "RSA";
  private static final int KEY_SIZE = 2048;

  private static final String KEY_PREFIX = "auth:keys:";
  private static final String CURRENT_KEY = "auth:keys:current";
  private static final String KEY_IDS = "auth:keys:ids";

  private static final String ROTATE_LOCK = "auth:keys:rotate-lock";
  private static final Duration ROTATE_LOCK_TTL = Duration.ofSeconds(30);

  private static final int WAIT_RETRY = 30;
  private static final Duration WAIT_DELAY = Duration.ofMillis(100);

  private final StringRedisTemplate redis;
  private final IdGenerator idGen;

  public RedisKeyProvider(StringRedisTemplate redisTemplate, IdGenerator idGenerator) {
    this.redis = redisTemplate;
    this.idGen = idGenerator;
  }

  @Override
  public RsaKeyPair getCurrent() {
    var keyValue = redis.opsForValue().get(CURRENT_KEY);

    if (keyValue == null || keyValue.isBlank()) {
      return rotate();
    }

    var key = parseKey(keyValue).flatMap(this::getById);

    if (key.isPresent()) {
      return key.get();
    }

    logger.warn("Current key ID {} not found, rotating keys, kid = {}", keyValue);
    return rotate();
  }

  @Override
  public Optional<RsaKeyPair> getById(Long kid) {
    if (kid == null) {
      return Optional.empty();
    }

    var privateValue = redis.opsForValue().get(privateKey(kid));
    var publicValue = redis.opsForValue().get(publicKey(kid));

    if (privateValue == null || publicValue == null) {
      return Optional.empty();
    }

    try {
      var privateKey = toPrivateKey(privateValue);
      var publicKey = toPublicKey(publicValue);
      return Optional.of(new RsaKeyPair(kid, privateKey, publicKey));
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      logger.warn("Invalid RSA key data in Redis. kid={}", kid, e);
      return Optional.empty();
    }
  }

  @Override
  public RsaKeyPair rotate() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
      generator.initialize(KEY_SIZE);

      var keys = generator.generateKeyPair();
      var pvk = (RSAPrivateKey) keys.getPrivate();
      var pub = (RSAPublicKey) keys.getPublic();
      var kid = nextKid();

      redis.opsForValue().set(privateKey(kid), Base64.encode(pvk.getEncoded()));
      redis.opsForValue().set(publicKey(kid), Base64.encode(pub.getEncoded()));
      redis.opsForSet().add(KEY_IDS, String.valueOf(kid));
      redis.opsForValue().set(CURRENT_KEY, String.valueOf(kid));

      logger.info("Rotated RSA keys successfully with kid = {}", kid);
      return new RsaKeyPair(kid, pvk, pub);
    } catch (GeneralSecurityException e) {
      throw new RuntimeException("Failed to rotate RSA keys", e);
    }
  }

  @Override
  public JWKSet jwkSet() {
    getCurrent(); // Ensure current key exists

    var ids = redis.opsForSet().members(KEY_IDS);

    if (ids == null || ids.isEmpty()) {
      return new JWKSet();
    }

    var keys = ids.stream()
        .map(this::parseKey)
        .flatMap(Optional::stream)
        .map(this::toJwk)
        .flatMap(Optional::stream)
        .toList();

    return new JWKSet(keys);
  }

  private Optional<JWK> toJwk(Long kid) {
    var publicValue = redis.opsForValue().get(publicKey(kid));

    if (publicValue == null) {
      return Optional.empty();
    }

    try {
      var pubKey = toPublicKey(publicValue);
      var jwk = new RSAKey.Builder(pubKey)
          .keyID(String.valueOf(kid))
          .keyUse(KeyUse.SIGNATURE)
          .algorithm(JWSAlgorithm.RS256)
          .build();

      return Optional.of(jwk);
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      logger.warn("Failed to build JWK. kid={}", kid, e);
      return Optional.empty();
    }
  }

  private Long nextKid() {
    return idGen.nextId().value();
  }

  private String privateKey(Long kid) {
    return KEY_PREFIX + kid + ":private";
  }

  private String publicKey(Long kid) {
    return KEY_PREFIX + kid + ":public";
  }

  private Optional<Long> parseKey(String keyValue) {
    try {
      return Optional.of(Long.valueOf(keyValue));
    } catch (NumberFormatException e) {
      logger.error("Invalid key ID format: {}", keyValue);
      return Optional.empty();
    }
  }

  private RSAPrivateKey toPrivateKey(String value) throws GeneralSecurityException {
    var spec = new PKCS8EncodedKeySpec(Base64.decode(value));
    return (RSAPrivateKey) KeyFactory.getInstance(RSA).generatePrivate(spec);
  }

  private RSAPublicKey toPublicKey(String value) throws GeneralSecurityException {
    var spec = new X509EncodedKeySpec(Base64.decode(value));
    return (RSAPublicKey) KeyFactory.getInstance(RSA).generatePublic(spec);
  }
}
