package com.phatpl.metube.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.phatpl.metube.auth.service.impl.RedisKeyProvider;
import com.phatpl.metube.common.id.SnowflakeProperties;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
public class AppConfig {

  private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

  private RedisKeyProvider keyProvider;

  public AppConfig(RedisKeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @PostConstruct
  public void init() {
    logger.info("Initializing application — rotating RSA keys...");
    keyProvider.rotate();
    logger.info("RSA key rotation complete.");
  }
}
