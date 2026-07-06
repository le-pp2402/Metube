package com.phatpl.metube.common.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
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

  /**
   * Writes a JSON:API error document containing multiple error objects.
   *
   * Per JSON:API §7.2, a document MAY contain multiple errors when a single
   * request produces several distinct failures — the primary use case here is
   * bean-validation where each invalid field generates its own error entry with
   * a source.pointer pointing at the offending attribute.
   *
   * errors — pre-built list, one JsonApiError per validation violation.
   * Must not be empty; callers should guard against empty lists.
   * status — HTTP status written to the response (typically 422).
   *
   * Logs only the first error to avoid noise; the HTTP status communicates
   * the overall outcome.
   */
  public void writeMany(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      List<JsonApiError> errors) throws IOException {
    if (response.isCommitted() || errors.isEmpty()) {
      return;
    }

    // Log the first error entry; individual field details are in the body
    errorLogger.log(request, status, ApiErrorCode.VALIDATION_FAILED, errors.get(0), null);

    response.resetBuffer();
    response.setStatus(status.value());
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(JSON_API_MEDIA_TYPE);

    // JsonApiErrorDocument already holds List<JsonApiError> — no new type needed
    var doc = new JsonApiErrorDocument(new JsonApiObject("1.1"), errors);
    objectMapper.writeValue(response.getOutputStream(), doc);

    response.flushBuffer();
  }
}
