package com.phatpl.metube.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metube.redis")
public record RedisProperties(String host, int port, String password) {
}