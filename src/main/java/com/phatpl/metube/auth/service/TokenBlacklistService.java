package com.phatpl.metube.auth.service;

import java.time.Instant;

public interface TokenBlacklistService {
  void revoke(Long jti, Instant expiry);

  boolean isRevoked(Long jti);
}
