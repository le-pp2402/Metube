package com.phatpl.metube.auth.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.api.JsonApiErrorWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JsonApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final JsonApiErrorWriter errorWriter;

  public JsonApiAuthenticationEntryPoint(JsonApiErrorWriter errorWriter) {
    this.errorWriter = errorWriter;
  }

  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException)
      throws IOException, ServletException {
    errorWriter.write(
        req,
        res,
        HttpStatus.UNAUTHORIZED,
        ApiErrorCode.AUTHENTICATION_REQUIRED,
        "Authentication required",
        authException.getMessage(),
        null,
        authException);
  }
}
