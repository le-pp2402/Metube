package com.phatpl.metube.common.web;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.phatpl.metube.common.api.ApiErrorBody;
import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.api.ApiErrorWriter;
import com.phatpl.metube.common.api.ApiResponse;
import com.phatpl.metube.common.api.ErrorDetail;
import com.phatpl.metube.common.exception.ApiException;
import com.phatpl.metube.common.exception.SchemaValidationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Central exception → HTTP response translator for all @RestController methods.
 *
 * The Spring Security filter layer (JwtAuthenticationFilter, etc.) handles its
 * own errors directly via {@link ApiErrorWriter} because those run before the
 * dispatcher servlet — they cannot reach this advice.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final ApiErrorWriter errorWriter;

  public GlobalExceptionHandler(ApiErrorWriter errorWriter) {
    this.errorWriter = errorWriter;
  }

  /**
   * Bean validation failure (@Valid on @RequestBody).
   * Each field violation becomes its own ErrorDetail entry.
   * HTTP 422 Unprocessable Content.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public void handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    List<ErrorDetail> details = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fe -> new ErrorDetail(fe.getField(), fe.getDefaultMessage()))
        .toList();

    errorWriter.writeValidation(req, res, HttpStatus.UNPROCESSABLE_CONTENT, details);
  }

  /**
   * Schema validation failure (custom @ValidateSchema annotation).
   */
  @ExceptionHandler(SchemaValidationException.class)
  public void handleSchemaValidation(
      SchemaValidationException ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    List<ErrorDetail> details = ex.getErrors().stream()
        .map(msg -> new ErrorDetail("body", msg))
        .toList();

    errorWriter.writeValidation(req, res, HttpStatus.UNPROCESSABLE_CONTENT, details);
  }

  /**
   * All domain errors (UsernameTakenException, etc.) — extend ApiException.
   */
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
    var errorBody = ApiErrorBody.of(ex.getCode(), ex.getSafeDetail());
    return ResponseEntity
        .status(ex.getStatus())
        .body(ApiResponse.failure(errorBody));
  }

  /**
   * Spring Security AuthenticationException caught at controller layer.
   * Returns 401 with a generic message — never echo ex.getMessage().
   */
  @ExceptionHandler(AuthenticationException.class)
  public void handleAuthentication(
      AuthenticationException ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    errorWriter.write(
        req, res,
        HttpStatus.UNAUTHORIZED,
        ApiErrorCode.AUTHENTICATION_REQUIRED,
        "Invalid credentials or session expired",
        ex);
  }

  /**
   * Spring Security AccessDeniedException caught at controller layer.
   * Returns 403.
   */
  @ExceptionHandler(AccessDeniedException.class)
  public void handleAccessDenied(
      AccessDeniedException ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    errorWriter.write(
        req, res,
        HttpStatus.FORBIDDEN,
        ApiErrorCode.ACCESS_DENIED,
        "You do not have permission to perform this action",
        ex);
  }

  /**
   * Catch-all. Logs full stack trace, returns a safe generic message.
   */
  @ExceptionHandler(Exception.class)
  public void handleUnexpected(
      Exception ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    log.error("Unhandled exception at {} {}", req.getMethod(), req.getRequestURI(), ex);

    errorWriter.write(
        req, res,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ApiErrorCode.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred. Please try again later.",
        ex);
  }
}
