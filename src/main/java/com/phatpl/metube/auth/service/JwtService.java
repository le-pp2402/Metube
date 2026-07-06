package com.phatpl.metube.auth.service;

import org.springframework.stereotype.Service;

import com.phatpl.metube.auth.model.TokenClaims;
import com.phatpl.metube.auth.model.UserPrincipal;

@Service
public interface JwtService {
  String genAccessToken(UserPrincipal user);

  String genRefreshToken(UserPrincipal user);

  TokenClaims validateToken(String token);

  void revokeToken(TokenClaims claims);
}