package com.phatpl.metube.auth.service.impl;

import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.phatpl.metube.auth.model.TokenClaims;
import com.phatpl.metube.auth.model.UserPrincipal;
import com.phatpl.metube.auth.service.JwtService;
import com.phatpl.metube.auth.service.KeyProvider;
import com.phatpl.metube.auth.service.TokenBlacklistService;
import com.phatpl.metube.common.JwtProperties;
import com.phatpl.metube.common.exception.InvalidTokenException;
import com.phatpl.metube.common.id.IdGenerator;

@Service
public class JwtServiceImpl implements JwtService {
  private static final String ACCESS = "access";
  private static final String REFRESH = "refresh";
  private static final String CLAIM_TYPE = "type";
  private static final String CLAIM_TOKEN_VER = "token_ver";
  private static final String CLAIM_USERNAME = "username";

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
    try {
      var jwt = SignedJWT.parse(token);
      var header = jwt.getHeader();

      validateAlgorithm(header);

      var kid = parseKid(header.getKeyID());

      var keyPair = keyProvider.getById(kid)
          .orElseThrow(() -> new InvalidTokenException("Unknow JWT kid: " + kid));

      var verifier = new RSASSAVerifier(keyPair.publicKey());
      if (!jwt.verify(verifier)) {
        throw new InvalidTokenException("Invalid JWT signature");
      }

      var claims = jwt.getJWTClaimsSet();

      validateRequiredClaims(claims);
      validateExpiration(claims);

      var jti = parseKid(claims.getJWTID());

      if (blackListService.isRevoked(jti)) {
        throw new InvalidTokenException("Token has been revoked");
      }

      return toTokenClaims(kid, claims);
    } catch (NumberFormatException | JOSEException | ParseException e) {
      throw new InvalidTokenException("Invalid JWT", e);
    }
  }

  private TokenClaims toTokenClaims(Long kid, JWTClaimsSet claims) throws ParseException {
    return new TokenClaims(
        Long.parseLong(claims.getSubject()),
        claims.getStringClaim(CLAIM_USERNAME),
        claims.getLongClaim(CLAIM_TOKEN_VER),
        Long.parseLong(claims.getJWTID()),
        claims.getExpirationTime().toInstant(),
        claims.getStringClaim(CLAIM_TYPE));
  }

  private void validateRequiredClaims(JWTClaimsSet claims) {
    if (claims.getSubject() == null || claims.getSubject().isBlank()) {
      throw new InvalidTokenException("Missing JWT subject");
    }

    if (claims.getJWTID() == null || claims.getJWTID().isBlank()) {
      throw new InvalidTokenException("Missing JWT jti");
    }

    try {
      var type = claims.getStringClaim(CLAIM_TYPE);
      if (!ACCESS.equals(type) && !REFRESH.equals(type)) {
        throw new InvalidTokenException("Invalid JWT type: " + type);
      }
    } catch (ParseException e) {
      throw new InvalidTokenException("Invalid JWT type", e);
    }
  }

  private void validateExpiration(JWTClaimsSet claims) {
    var exp = claims.getExpirationTime();

    if (exp == null) {
      throw new InvalidTokenException("Missing JWT expiration");
    }

    if (!exp.after(new Date())) {
      throw new InvalidTokenException("JWT has expired");
    }
  }

  private void validateAlgorithm(JWSHeader header) {
    if (!header.getAlgorithm().equals(JWSAlgorithm.RS256)) {
      throw new InvalidTokenException("Invalid token algorithm");
    }
  }

  private Long parseKid(String kid) {
    if (kid == null || kid.isBlank()) {
      throw new InvalidTokenException("Missing JWT kid");
    }

    try {
      return Long.valueOf(kid);
    } catch (NumberFormatException e) {
      throw new InvalidTokenException("Invalid JWT kid: " + kid, e);
    }
  }

  @Override
  public void revokeToken(TokenClaims claims) {
    if (claims == null) {
      return;
    }
    blackListService.revoke(claims.jti(), claims.expiry());
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
        .claim(CLAIM_USERNAME, user.getUsername())
        .claim(CLAIM_TOKEN_VER, user.getTokenVer())
        .claim(CLAIM_TYPE, type)
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
