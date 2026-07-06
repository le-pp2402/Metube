package com.phatpl.metube.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phatpl.metube.common.CorsProperties;
import com.phatpl.metube.auth.service.impl.RedisKeyProvider;
import com.phatpl.metube.common.JwtProperties;
import com.phatpl.metube.common.id.SnowflakeProperties;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties({ SnowflakeProperties.class, JwtProperties.class, CorsProperties.class })
public class AppConfig {

  private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

  private RedisKeyProvider keyProvider;

  public AppConfig(RedisKeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @Async
  @PostConstruct
  public void init() {
    logger.info("Initializing application — rotating RSA keys...");
    keyProvider.getCurrent();
    logger.info("RSA key rotation complete.");
  }

  /**
   * Canonical ObjectMapper bean for the entire application.
   *
   * spring-boot-starter-webmvc does not automatically activate
   * JacksonAutoConfiguration the way spring-boot-starter-web does,
   * so we declare the bean explicitly here.
   *
   * FAIL_ON_UNKNOWN_PROPERTIES = false — tolerate extra fields in incoming
   *   JSON (forward-compatible: old server handles new client payloads).
   * NON_NULL inclusion — null fields are omitted from responses, keeping
   *   JSON:API error documents clean (no "source": null noise).
   */
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // setDefaultPropertyInclusion replaces deprecated setSerializationInclusion.
        // Both set global serialization inclusion; NON_NULL omits null fields from
        // responses — keeps JSON:API error documents clean (no "source": null).
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
  }
}
