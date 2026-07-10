package com.phatpl.metube.common.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.phatpl.metube.common.RedisProperties;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

  @Bean
  public LettuceConnectionFactory redisConnectionFactory(
      RedisProperties redisProperties) {
    var config = LettuceClientConfiguration.builder()
        .commandTimeout(Duration.ofSeconds(2)).shutdownTimeout(Duration.ZERO)
        .build();

    var redisConFactory = new RedisStandaloneConfiguration();

    redisConFactory.setHostName(redisProperties.host());
    redisConFactory.setPort(redisProperties.port());
    redisConFactory.setPassword(RedisPassword.of(redisProperties.password()));

    return new LettuceConnectionFactory(redisConFactory, config);
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate(
      LettuceConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }
}
