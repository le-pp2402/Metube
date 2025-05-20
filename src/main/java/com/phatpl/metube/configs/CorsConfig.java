package com.phatpl.metube.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {

                String[] allowedOrigins = new String[]{"http://localhost:3000", "http://localhost:81"} ;

                registry
                        .addMapping("/**")
                        .allowedOrigins(allowedOrigins)
                        .allowedMethods(
                                RequestMethod.GET.name(),
                                RequestMethod.POST.name(),
                                RequestMethod.DELETE.name(),
                                RequestMethod.OPTIONS.name()
                        )
                        .allowedHeaders("*")
                        .allowedOriginPatterns(allowedOrigins)
                        .allowCredentials(true).maxAge(3600);
            }
        };
    }
}
