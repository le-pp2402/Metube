package com.phatpl.metube.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import io.github.lepp2402.core.ChallengeData;
import io.github.lepp2402.core.ChallengeStore;
import io.github.lepp2402.service.POWService;
import io.github.lepp2402.storage.RedisChallengeStorage;

@Configuration
public class CaptchaConfig {
    @Bean
    public RedisTemplate<String, ChallengeData> challengRedisTemplate(LettuceConnectionFactory con) {
        var challengeTemplate = new RedisTemplate<String, ChallengeData>();
        challengeTemplate.setConnectionFactory(con);
        return challengeTemplate;
    }

    @Bean
    public POWService powService(RedisTemplate<String, ChallengeData> redisTemplate) {
        ChallengeStore store = new RedisChallengeStorage(redisTemplate);
        return new POWService(store);
    }
}
