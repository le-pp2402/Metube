package com.phatpl.metube.auth.service;

import com.phatpl.metube.auth.model.TokenClaims;
import com.phatpl.metube.auth.model.UserPrincipal;

public interface JwtService {
  String genAccessToken(UserPrincipal user);

  String genRefreshToken(UserPrincipal user);

  TokenClaims validateToken(String token);

  void revokeToken(TokenClaims claims);
}