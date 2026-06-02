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

  public JsonApiErrorWriter(ObjectMapper objectMapper, JsonApiErrorFactory errorFactory) {
    this.objectMapper = objectMapper;
    this.errorFactory = errorFactory;
  }

  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorCode code,
      String title,
      String detail,
      JsonApiErrorSource source) throws IOException {
    if (response.isCommitted()) {
      return;
    }

    response.resetBuffer();
    response.setStatus(status.value());
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(JSON_API_MEDIA_TYPE);

    var error = errorFactory.create(request, status, code, title, detail, source);

    objectMapper.writeValue(response.getOutputStream(), errorFactory.document(error));

    response.flushBuffer();
  }
}
