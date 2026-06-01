package com.phatpl.metube.auth.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.phatpl.metube.auth.service.JwtService;
import com.phatpl.metube.auth.service.impl.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String BEARER_PERFIX = "Bearer ";
  private static final String ACCESS = "access";

  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;

  public JwtAuthenticationFilter(JwtService jwtService, UserDetailsServiceImpl userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
        
  }

  private String extractBearerToken(HttpServletRequest req) {
    var author = req.getHeader(HttpHeaders.AUTHORIZATION);

    if (author == null || !author.startsWith(BEARER_PERFIX)) {
      return null;
    }

    var token = author.substring(BEARER_PERFIX.length()).trim();

    return token.isBlank() ? null : token;
  }
}
