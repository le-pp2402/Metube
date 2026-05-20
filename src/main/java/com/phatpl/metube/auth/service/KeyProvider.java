package com.phatpl.metube.auth.service;

import java.util.Optional;

import com.nimbusds.jose.jwk.JWKSet;
import com.phatpl.metube.auth.model.RsaKeyPair;

public interface KeyProvider {
  RsaKeyPair getCurrent();

  Optional<RsaKeyPair> getById(Long kid);

  RsaKeyPair rotate();

  JWKSet jwkSet();
}
