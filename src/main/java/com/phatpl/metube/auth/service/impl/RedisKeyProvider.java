package com.phatpl.metube.auth.service.impl;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
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

  // RSA 256
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

  private static final RedisScript<Long> UNLOCK_SCRIPT = RedisScript.of(
      """
          if redis.call('get', KEYS[1]) == ARGV[1] then
            return redis.call('del', KEYS[1])
          end
          return 0
          """, Long.class);

  // Gets the current RSA key pair. If there is no current key, it will attempt to
  // rotate keys.
  @Override
  public RsaKeyPair getCurrent() {
    return findCurrent().orElseGet(() -> {
      logger.info("No current RSA key found, rotating keys");
      return rotateIfMissing();
    });
  }

  private RsaKeyPair rotateIfMissing() {
    var token = idGen.nextId().toString();

    if (!tryLock(token)) {
      return waitForCurrentKey();
    }

    try {
      return findCurrent().orElseGet(this::doRotate);
    } finally {
      unlock(token);
    }
  }

  // Gets the RSA key pair by its key ID (kid).
  // Returns an empty Optional if the key is not found or if there is an error
  // parsing the key data.
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

  // Rotates the RSA keys by generating a new key pair,
  // storing them in Redis, and updating the current key ID.
  @Override
  public RsaKeyPair rotate() {
    var token = idGen.nextId().toString();

    if (!tryLock(token)) {
      return waitForRotationDone();
    }

    try {
      return doRotate();
    } finally {
      unlock(token);
    }
  }

  private RsaKeyPair doRotate() {
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
      throw new IllegalStateException("Failed to rotate RSA keys", e);
    }
  }

  // Builds a JWKSet containing all the public keys available in Redis.
  @Override
  public JWKSet jwkSet() {
    ensureCurrentKey();

    var ids = redis.opsForSet().members(KEY_IDS);

    if (ids == null || ids.isEmpty()) {
      return new JWKSet();
    }

    var kids = ids.stream()
        .map(this::parseKey)
        .flatMap(Optional::stream)
        .toList();

    var redisKeys = kids.stream()
        .map(this::publicKey)
        .toList();

    if (redisKeys.isEmpty()) {
      return new JWKSet();
    }

    var values = redis.opsForValue().multiGet(redisKeys);

    if (values == null || values.isEmpty()) {
      return new JWKSet();
    }

    var keys = buildJwks(kids, values);

    return new JWKSet(keys);
  }

  // Helper method to build a list of JWKs from the given key IDs and their
  // corresponding public key values.
  private List<JWK> buildJwks(List<Long> keyIds, List<String> values) {
    var keys = new ArrayList<JWK>();
    int size = Math.min(keyIds.size(), values.size());

    for (int i = 0; i < size; i++) {
      var publicValue = values.get(i);

      if (publicValue == null) {
        continue;
      }

      toJwk(keyIds.get(i), publicValue).ifPresent(keys::add);
    }

    return keys;
  }

  // Finds the current RSA key pair. Returns an empty Optional if no current key
  // is found.
  private Optional<RsaKeyPair> findCurrent() {
    var keyValue = redis.opsForValue().get(CURRENT_KEY);

    if (keyValue == null || keyValue.isBlank()) {
      return Optional.empty();
    }

    return parseKey(keyValue).flatMap(this::getById);
  }

  // Helper method to convert a public key value from Redis into a JWK.
  // Returns an empty Optional if there is an error during conversion.
  private Optional<JWK> toJwk(Long kid, String publicValue) {
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

  // Ensures that there is a current key available. If not, it will attempt to
  // rotate keys.
  private void ensureCurrentKey() {
    if (findCurrent().isEmpty()) {
      rotateIfMissing();
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
      logger.warn("Invalid key ID format: {}", keyValue);
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

  private boolean tryLock(String token) {
    return Boolean.TRUE.equals(
        redis.opsForValue().setIfAbsent(ROTATE_LOCK, token, ROTATE_LOCK_TTL));
  }

  private boolean unlock(String token) {
    var result = redis.execute(UNLOCK_SCRIPT, List.of(ROTATE_LOCK), token);
    return Long.valueOf(1L).equals(result);
  }

  private RsaKeyPair waitForCurrentKey() {
    for (int i = 0; i < WAIT_RETRY; i++) {
      sleep(WAIT_DELAY);

      var current = findCurrent();
      if (current.isPresent()) {
        return current.get();
      }
    }

    throw new IllegalStateException("Timed out waiting for current RSA key");
  }

  // Waits for the RSA key rotation to complete by periodically checking if the
  // rotation lock has been released and if the current key is available.
  private RsaKeyPair waitForRotationDone() {
    for (int i = 0; i < WAIT_RETRY; i++) {
      sleep(WAIT_DELAY);

      if (!Boolean.TRUE.equals(redis.hasKey(ROTATE_LOCK))) {
        return findCurrent()
            .orElseThrow(() -> new IllegalStateException("RSA rotation finished but current key is missing"));
      }
    }

    throw new IllegalStateException("Timed out waiting for RSA key rotation");
  }

  // Pause the current thread for the specified duration. If interrupted, it will
  // restore
  private void sleep(Duration duration) {
    try {
      Thread.sleep(duration.toMillis());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for RSA key rotation", e);
    }
  }
}
