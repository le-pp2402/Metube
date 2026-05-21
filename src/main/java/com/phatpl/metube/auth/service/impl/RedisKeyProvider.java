package com.phatpl.metube.auth.service.impl;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWKSet;
import com.phatpl.metube.auth.model.RsaKeyPair;
import com.phatpl.metube.auth.service.KeyProvider;
import com.phatpl.metube.common.id.IdGenerator;

@Component
public class RedisKeyProvider implements KeyProvider {
  private static final Logger logger = LoggerFactory.getLogger(RedisKeyProvider.class);
  private static final String KEY_PREFIX = "auth:keys:";
  private static final String CURRENT_KEY = "auth:keys:current";
  private static final String RSA = "RSA";

  private StringRedisTemplate redis;
  private static IdGenerator idGen;

  public RedisKeyProvider(StringRedisTemplate redisTemplate, IdGenerator idGenerator) {
    this.redis = redisTemplate;
    RedisKeyProvider.idGen = idGenerator;
  }

  @Override
  public RsaKeyPair getCurrent() {
    var keyId = redis.opsForValue().get(CURRENT_KEY);

    if (keyId != null) {
      var pvk = getPrivateKey(Long.valueOf(keyId));
      var pub = getPublicKey(Long.valueOf(keyId));

      if (pvk != null && pub != null) {
        return new RsaKeyPair(Long.valueOf(keyId), pvk, pub);
      }
    }

    var key = rotate(); // new fail show log then throw exception
    if (key == null) {
      throw new IllegalStateException("Failed to rotate RSA keys");
    }
    return key;
  }

  @Override
  public Optional<RsaKeyPair> getById(Long kid) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getById'");
  }

  @Override
  public RsaKeyPair rotate() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA);
      keyGen.initialize(2048);
      var keys = keyGen.generateKeyPair();
      var pvk = (RSAPrivateKey) keys.getPrivate();
      var pub = (RSAPublicKey) keys.getPublic();
      var kid = genKeyId();
      logger.info("pvk {}", pvk.getEncoded().toString());
      redis.opsForValue().set(KEY_PREFIX + kid + ":private", new String(pvk.getEncoded()));
      redis.opsForValue().set(KEY_PREFIX + kid + ":public", new String(pub.getEncoded()));
      redis.opsForValue().set(CURRENT_KEY, String.valueOf(kid));
      return new RsaKeyPair(kid, pvk, pub);
    } catch (Exception e) {
      logger.error("Error rotating RSA keys: {}", e.getMessage());
    }
    return null;
  }

  @Override
  public JWKSet jwkSet() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'jwkSet'");
  }

  private Long genKeyId() {
    return idGen.nextId().value();
  }

  private RSAPrivateKey getPrivateKey(Long kid) {
    var pkeyPem = redis.opsForValue().get(KEY_PREFIX + kid + ":private");
    if (pkeyPem == null)
      return null;
    byte[] encoded = pkeyPem.getBytes();

    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    try {
      KeyFactory kf = KeyFactory.getInstance(RSA);
      return (RSAPrivateKey) kf.generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      logger.error("Error generating RSA private key: {}", e.getMessage());
    }
    return null;
  }

  private RSAPublicKey getPublicKey(Long kid) {
    var pkeyPem = redis.opsForValue().get(KEY_PREFIX + kid + ":public");
    if (pkeyPem == null)
      return null;
    byte[] encoded = pkeyPem.getBytes();

    try {
      KeyFactory kf = KeyFactory.getInstance(RSA);
      return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      logger.error("Error generating RSA public key: {}", e.getMessage());
    }
    return null;
  }
}
