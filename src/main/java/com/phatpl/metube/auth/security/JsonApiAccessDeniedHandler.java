package com.phatpl.metube.auth.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.api.ApiErrorWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JsonApiAccessDeniedHandler implements AccessDeniedHandler {
  private final ApiErrorWriter errorWriter;

  public JsonApiAccessDeniedHandler(ApiErrorWriter errorWriter) {
    this.errorWriter = errorWriter;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {

    errorWriter.write(
        request,
        response,
        HttpStatus.FORBIDDEN,
        ApiErrorCode.ACCESS_DENIED,
        "You do not have permission to perform this action",
        accessDeniedException);
  }
}
