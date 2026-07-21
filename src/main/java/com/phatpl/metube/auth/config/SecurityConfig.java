package com.phatpl.metube.auth.config;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.context.annotation.Primary;
import com.phatpl.metube.auth.security.JsonApiAccessDeniedHandler;
import com.phatpl.metube.auth.security.JsonApiAuthenticationEntryPoint;
import com.phatpl.metube.auth.security.JwtAuthenticationFilter;
import com.phatpl.metube.common.CorsProperties;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtFilter;
  private static final int PASSWORD_ENCODER_STRENGTH = 10;

  public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
    this.jwtFilter = jwtFilter;
  }

  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
    FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JsonApiAuthenticationEntryPoint entryPoint,
      JsonApiAccessDeniedHandler accessDeniedHandler,
      CorsConfigurationSource corsSource) throws Exception {

    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName(null);

    // CSRF: enabled via double-submit cookie pattern.
    // CookieCsrfTokenRepository stores the CSRF token in a cookie named XSRF-TOKEN.
    // withHttpOnlyFalse() lets JavaScript read that cookie and attach it as
    // X-XSRF-TOKEN header on POST/PUT/PATCH/DELETE.
    http.csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(requestHandler));

    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.cors(cors -> cors.configurationSource(corsSource));

    http.authorizeHttpRequests(
        auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
            .requestMatchers("/api/v1/auth/**").permitAll()
            .anyRequest().authenticated());

    http.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler));

    // filter order
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  // encoder for user password
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(PASSWORD_ENCODER_STRENGTH);
  }

  // init list of auth provider
  @Bean
  public AuthenticationManager authenticationManager(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);

    return new ProviderManager(authProvider);
  }

  @Bean
  @Primary
  // @Primary: Spring MVC auto-configuration also registers a
  // CorsConfigurationSource
  // (mvcHandlerMappingIntrospector). @Primary tells Spring to prefer our bean
  // when
  // injecting by type into filterChain.
  public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
    var config = new CorsConfiguration();

    config.setAllowedOrigins(corsProperties.allowedOrigins());
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
    config.setExposedHeaders(List.of("Authorization", "X-Request_id", "X-XSRF-TOKEN"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }
}
