package com.phatpl.metube.auth.service;

import java.time.Instant;

public interface TokenBlacklistService {
  void add(Long jti, Instant expiry);

  boolean isBlacklisted(Long jti);
}
