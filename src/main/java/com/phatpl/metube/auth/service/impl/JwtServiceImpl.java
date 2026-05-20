package com.phatpl.metube.auth.service.impl;

import com.phatpl.metube.auth.model.TokenClaims;
import com.phatpl.metube.auth.model.UserPrincipal;
import com.phatpl.metube.auth.service.JwtService;

public class JwtServiceImpl implements JwtService {

  @Override
  public String genAccessToken(UserPrincipal user) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'genAccessToken'");
  }

  @Override
  public String genRefreshToken(UserPrincipal user) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'genRefreshToken'");
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

}
