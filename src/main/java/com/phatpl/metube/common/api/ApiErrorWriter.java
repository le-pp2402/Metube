package com.phatpl.metube.common.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phatpl.metube.common.logging.LogMdcKeys;

import cn.hutool.core.util.IdUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Writes {@link ApiResponse} error envelopes to the HTTP response stream.
 *
 * Used by both the Spring Security filter layer (which cannot reach
 * {@code GlobalExceptionHandler}) and the exception handler itself.
 */
@Component
public class ApiErrorWriter {

  private final ObjectMapper objectMapper;
  private final ApiErrorLogger errorLogger;

  public ApiErrorWriter(ObjectMapper objectMapper, ApiErrorLogger errorLogger) {
    this.objectMapper = objectMapper;
    this.errorLogger = errorLogger;
  }

  /**
   * Writes a single error response.
   *
   * @param request   incoming HTTP request (for logging and meta)
   * @param response  outgoing HTTP response to write into
   * @param status    HTTP status code
   * @param code      machine-readable error code
   * @param message   human-readable message for the end-user
   * @param throwable optional causing exception (for logging only)
   */
  public void write(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorCode code,
      String message,
      Throwable throwable) throws IOException {
    write(request, response, status, ApiErrorBody.of(code, message), throwable);
  }

  /**
   * Writes a validation error response with multiple field-level details.
   *
   * @param details one entry per constraint violation
   */
  public void writeValidation(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      List<ErrorDetail> details) throws IOException {

    var errorBody = ApiErrorBody.of(ApiErrorCode.VALIDATION_FAILED, "Request validation failed", details);
    write(request, response, status, errorBody, null);
  }

  private void write(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpStatus status,
      ApiErrorBody errorBody,
      Throwable throwable) throws IOException {

    if (response.isCommitted()) {
      return;
    }

    String requestId = IdUtil.fastUUID();
    MDC.put(LogMdcKeys.ERROR_ID, requestId);

    try {
      errorLogger.log(request, status, errorBody.code(), throwable);

      var meta = new ErrorMeta(requestId, request.getRequestURI(), request.getMethod());
      var apiResponse = ApiResponse.failure(errorBody, meta);

      response.resetBuffer();
      response.setStatus(status.value());
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      objectMapper.writeValue(response.getOutputStream(), apiResponse);
      response.flushBuffer();

    } finally {
      MDC.remove(LogMdcKeys.ERROR_ID);
    }
  }

  /** Meta block attached to every error response */
  private record ErrorMeta(String requestId, String path, String method) {
  }
}
