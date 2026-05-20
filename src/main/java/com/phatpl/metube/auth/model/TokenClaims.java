package com.phatpl.metube.auth.model;

import java.time.Instant;

public record TokenClaims(
    Long userId,
    String username,
    Long tokenVer,
    Long jti,
    Instant expiry,
    String type) {
  public static final String ACCESS_TOKEN_TYPE = "access";
  public static final String REFRESH_TOKEN_TYPE = "refresh";

  public boolean isAccess() {
    return ACCESS_TOKEN_TYPE.equals(type);
  }
}