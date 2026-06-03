package com.phatpl.metube.common.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonApiErrorWriter {
  public static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";

  private final ObjectMapper objectMapper;
  private final JsonApiErrorFactory errorFactory;
  private final JsonApiErrorLogger errorLogger;

  public JsonApiErrorWriter(ObjectMapper objectMapper, JsonApiErrorFactory errorFactory,
      JsonApiErrorLogger errorLogger) {
    this.objectMapper = objectMapper;
    this.errorFactory = errorFactory;
    this.errorLogger = errorLogger;
  }

  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorCode code,
      String title,
      String detail,
      JsonApiErrorSource source)
      throws IOException {
    write(request, response, status, code, title, detail, source, null);
  }

  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorCode code,
      String title,
      String detail,
      JsonApiErrorSource source,
      Throwable throwable) throws IOException {
    if (response.isCommitted()) {
      return;
    }

    var error = errorFactory.create(request, status, code, title, detail, source);
    errorLogger.log(request, status, code, error, throwable);
    
    response.resetBuffer();
    response.setStatus(status.value());
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(JSON_API_MEDIA_TYPE);

    objectMapper.writeValue(response.getOutputStream(), errorFactory.document(error));

    response.flushBuffer();
  }
}
