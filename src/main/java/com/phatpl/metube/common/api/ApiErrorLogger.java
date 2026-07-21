package com.phatpl.metube.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Logs API errors at the appropriate level:
 * - 5xx → ERROR (with stack trace)
 * - 4xx with exception → WARN (message only)
 * - 4xx without exception → WARN (no exception detail)
 */
@Component
public class ApiErrorLogger {
  private static final Logger logger = LoggerFactory.getLogger(ApiErrorLogger.class);

  public void log(
      HttpServletRequest request,
      HttpStatus status,
      String code,
      Throwable throwable) {

    if (status.is5xxServerError()) {
      logger.error(
          "API error: status={}, code={}, method={}, path={}",
          status.value(), code,
          request.getMethod(), request.getRequestURI(),
          throwable);
      return;
    }

    if (throwable != null) {
      logger.warn(
          "API error: status={}, code={}, method={}, path={}, message={}",
          status.value(), code,
          request.getMethod(), request.getRequestURI(),
          throwable.getMessage());
    } else {
      logger.warn(
          "API error: status={}, code={}, method={}, path={}",
          status.value(), code,
          request.getMethod(), request.getRequestURI());
    }
  }
}
