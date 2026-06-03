package com.phatpl.metube.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import com.phatpl.metube.common.logging.LogMdcKeys;

import jakarta.servlet.http.HttpServletRequest;

public class JsonApiErrorLogger {
  private static final Logger logger = LoggerFactory.getLogger(JsonApiErrorLogger.class);

  public void log(
      HttpServletRequest request,
      HttpStatus status,
      ApiErrorCode code,
      JsonApiError error,
      Throwable throwable) {

    MDC.put(LogMdcKeys.ERROR_ID, error.id());

    try {
      if (status.is5xxServerError()) {
        logger.error(
            "API error: errorId={}, status={}, code={}, method={}, path={}",
            error.id(),
            status.value(),
            code.name(),
            request.getMethod(),
            request.getRequestURI(),
            throwable);
        return;
      }

      if (throwable == null) {
        logger.warn(
            "API error: errorId={}, status={}, code={}, method={}, path={}",
            error.id(),
            status.value(),
            code.name(),
            request.getMethod(),
            request.getRequestURI());
        return;
      }

      logger.warn(
          "API error: errorId={}, status={}, code={}, method={}, path={}, message={}",
          error.id(),
          status.value(),
          code.name(),
          request.getMethod(),
          request.getRequestURI(),
          throwable.getMessage());

      logger.debug(
          "API error stacktrace: errorId={}",
          error.id(),
          throwable);
    } finally {
      MDC.remove(LogMdcKeys.ERROR_ID);
    }
  }
}
