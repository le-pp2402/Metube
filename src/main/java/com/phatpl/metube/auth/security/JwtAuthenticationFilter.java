package com.phatpl.metube.auth.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.phatpl.metube.auth.service.JwtService;
import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.api.JsonApiErrorWriter;
import com.phatpl.metube.common.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  // Name of the HttpOnly cookie that holds the JWT access token.
  // Must match the cookie name set by AuthService when issuing tokens.
  public static final String ACCESS_TOKEN_COOKIE = "access_token";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ACCESS = "access";

  private final JwtService jwtService;
  private final JsonApiErrorWriter errorWriter;

  public JwtAuthenticationFilter(JwtService jwtService, JsonApiErrorWriter errorWriter) {
    this.jwtService = jwtService;
    this.errorWriter = errorWriter;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    try {
      var token = extractToken(req);

      if (token == null) {
        filterChain.doFilter(req, response);
        return;
      }

      var claims = jwtService.validateToken(token);

      if (!ACCESS.equals(claims.type())) {
        throw new ApiAuthException(
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
    } catch (ApiAuthException | InvalidTokenException e) {
      ApiErrorCode errorCode = ApiErrorCode.INVALID_TOKEN;
      String detail = e.getMessage();
      if (e instanceof ApiAuthException aee) {
        errorCode = aee.getCode();
        detail = aee.getSafeDetail();
      }

      errorWriter.write(
          req,
          response,
          HttpStatus.UNAUTHORIZED,
          errorCode,
          "Authentication Failed",
          detail,
          null,
          e);
    }
  }

  /**
   * Extracts the JWT access token from the request.
   *
   * Priority:
   * 1. HttpOnly cookie named "access_token" — preferred when using cookie-based
   * auth.
   * HttpOnly prevents JS from reading it (XSS protection), CSRF protection covers
   * mutating requests.
   * 2. Authorization: Bearer <token> header — fallback for API clients (curl,
   * mobile).
   *
   * Returns null if no token is found, which causes the filter to pass the
   * request
   * through unauthenticated (Spring Security will then enforce rules per
   * endpoint).
   */
  private String extractToken(HttpServletRequest req) {
    // 1. Try HttpOnly cookie first
    if (req.getCookies() != null) {
      for (var cookie : req.getCookies()) {
        if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
          var value = cookie.getValue();
          return (value == null || value.isBlank()) ? null : value;
        }
      }
    }

    // 2. Fall back to Authorization: Bearer <token> header
    var header = req.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      var token = header.substring(BEARER_PREFIX.length()).trim();
      return token.isBlank() ? null : token;
    }

    return null;
  }
}
