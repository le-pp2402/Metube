package com.phatpl.metube.common;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metube.jwt")
public record JwtProperties(
        Duration accessTokenValidity,
        Duration refreshTokenValidity) {
}
