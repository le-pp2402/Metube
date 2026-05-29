package com.phatpl.metube.auth.service.impl;

import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.phatpl.metube.auth.model.TokenClaims;
import com.phatpl.metube.auth.model.UserPrincipal;
import com.phatpl.metube.auth.service.JwtService;
import com.phatpl.metube.auth.service.KeyProvider;
import com.phatpl.metube.auth.service.TokenBlacklistService;
import com.phatpl.metube.common.JwtProperties;
import com.phatpl.metube.common.id.IdGenerator;

@Service
public class JwtServiceImpl implements JwtService {
  private static final String ACCESS = "access";
  private static final String REFRESH = "refresh";
  private static final String TOKEN_TYPE_CLAIM = "type";
  private static final String TOKEN_VER_CLAIM = "token_ver";
  private static final String USERNAME_CLAIM = "username";

  private final KeyProvider keyProvider;
  private final TokenBlacklistService blackListService;
  private final IdGenerator idGenerator;
  private final JwtProperties props;

  public JwtServiceImpl(
      KeyProvider keyProvider,
      TokenBlacklistService blackListService,
      IdGenerator idGenerator,
      JwtProperties props) {
    this.keyProvider = keyProvider;
    this.blackListService = blackListService;
    this.idGenerator = idGenerator;
    this.props = props;
  }

  @Override
  public String genAccessToken(UserPrincipal user) {
    return genToken(user, ACCESS, props.accessTokenValidity());
  }

  @Override
  public String genRefreshToken(UserPrincipal user) {
    return genToken(user, REFRESH, props.refreshTokenValidity());
  }

  @Override
  public TokenClaims validateToken(String token) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'validateToken'");
  }

  @Override
  public void revokeToken(TokenClaims claims) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'revokeToken'");
  }

  private String genToken(UserPrincipal userPrincipal, String type, Duration validity) {
    var key = keyProvider.getCurrent();
    var user = userPrincipal.getUser();

    var now = Instant.now();
    var exp = now.plus(validity);
    JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
        .keyID(key.kid().toString())
        .build();

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .jwtID(idGenerator.nextId().toString())
        .subject(userPrincipal.getUsername())
        .issueTime(Date.from(now))
        .expirationTime(Date.from(exp))
        .claim(USERNAME_CLAIM, user.getUsername())
        .claim(TOKEN_VER_CLAIM, user.getTokenVer())
        .claim(TOKEN_TYPE_CLAIM, type)
        .build();

    return sign(header, claims, key.privateKey());
  }

  private String sign(JWSHeader header, JWTClaimsSet claims, RSAPrivateKey privateKey) {
    var jwt = new SignedJWT(header, claims);
    try {
      jwt.sign(new RSASSASigner(privateKey));
      return jwt.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to sign JWT", e);
    }
  }
}
