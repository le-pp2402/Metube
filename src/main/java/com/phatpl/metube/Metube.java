package com.phatpl.metube;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class Metube {
    public static void main(String[] args) {
        SpringApplication.run(Metube.class, args);
    }
}
