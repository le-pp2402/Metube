package com.phatpl.metube.auth.security;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.phatpl.metube.auth.service.JwtService;
import com.phatpl.metube.auth.service.impl.UserDetailsServiceImpl;
import com.phatpl.metube.common.api.ApiErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String BEARER_PERFIX = "Bearer ";
  private static final String ACCESS = "access";

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    try {
      var token = extractBearerToken(req);

      if (token == null) {
        filterChain.doFilter(req, response);
        return;
      }

      var claims = jwtService.validateToken(token);

      if (!ACCESS.equals(claims.type())) {
        throw new ApiAuthenticationException(
            ApiErrorCode.INVALID_TOKEN_TYPE,
            "Only access token is allowed for this secured");
      }

      // using claims as principal
      var authen = new UsernamePasswordAuthenticationToken(
          claims,
          null,
          List.of(new SimpleGrantedAuthority("ROLE_USER")));

      authen.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(req));

      SecurityContextHolder.getContext().setAuthentication(authen);

      filterChain.doFilter(req, response);
    } catch (Exception e) {
    }

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
