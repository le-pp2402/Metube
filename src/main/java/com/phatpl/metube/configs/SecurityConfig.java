package com.phatpl.metube.configs;

import com.phatpl.metube.filters.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${SECRET_KEY}")
    private String secretKey;
    private final String[] GET_PERMIT_ALL_URL = { "/resources", "/resources/{spring:[0-9]+}", "/video/**", "/video", "/live/**", "/ws/**", "/ws", "/live", "/live-session", "/live-session/**"};
    private final String[] POST_PERMIT_ALL_URL = {"/register", "/words/w/**", "/resources", "/video/**", "/live", "/live/**", "/login", "/video"};
    private final String[] PUT_PERMIT_ALL_URL = {"/verify"};

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(HttpMethod.GET, GET_PERMIT_ALL_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, POST_PERMIT_ALL_URL).permitAll()
                        .requestMatchers(HttpMethod.PUT, PUT_PERMIT_ALL_URL).permitAll()
                        .anyRequest().authenticated()
                );

        http.cors(Customizer.withDefaults());
        http.oauth2ResourceServer(auth -> auth.jwt(
                jwtDecoder -> jwtDecoder.decoder(jwtDecoder())
        ));

        http.authenticationProvider(authenticationProvider);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HS512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

}
