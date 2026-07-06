package com.phatpl.metube;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration;
import org.springframework.boot.servlet.autoconfigure.MultipartAutoConfiguration;
import org.springframework.boot.transaction.jta.autoconfigure.JtaAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {
        ErrorMvcAutoConfiguration.class,
        JtaAutoConfiguration.class,
        DataRedisReactiveAutoConfiguration.class,
        MultipartAutoConfiguration.class
})
public class Metube {
    public static void main(String[] args) {
        SpringApplication.run(Metube.class, args);
    }
}
