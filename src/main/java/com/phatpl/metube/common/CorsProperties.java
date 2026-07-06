package com.phatpl.metube.common;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "metube.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
