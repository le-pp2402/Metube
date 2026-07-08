package com.phatpl.metube.auth.service;

import org.springframework.data.redis.core.RedisTemplate;

public class AuthService {
    JwtService jwtService;
    RedisTemplate<String, Integer> loginFailedCounter;
}
