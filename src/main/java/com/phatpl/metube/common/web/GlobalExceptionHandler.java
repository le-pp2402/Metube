package com.phatpl.metube.common.web;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.api.JsonApiError;
import com.phatpl.metube.common.api.JsonApiErrorFactory;
import com.phatpl.metube.common.api.JsonApiErrorSource;
import com.phatpl.metube.common.api.JsonApiErrorWriter;
import com.phatpl.metube.common.exception.ApiException;
import com.phatpl.metube.common.exception.SchemaValidationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Central exception → HTTP response translator for all @RestController methods.
 *
 * Spring MVC calls this after a controller throws an exception. The filter
 * layer (JwtAuthenticationFilter, RateLimitFilter) handles its own errors
 * directly via JsonApiErrorWriter because those run before the dispatcher
 * servlet — they cannot reach this advice.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final JsonApiErrorWriter errorWriter;
  private final JsonApiErrorFactory errorFactory;

  public GlobalExceptionHandler(JsonApiErrorWriter errorWriter,
      JsonApiErrorFactory errorFactory) {
    this.errorWriter = errorWriter;
    this.errorFactory = errorFactory;
  }

  /**
   * Bean validation failure (@Valid on @RequestBody).
   *
   * MethodArgumentNotValidException carries a BindingResult with one
   * FieldError per constraint violation. Each violation becomes its own
   * JsonApiError with a source.pointer so the client knows exactly which
   * field to fix, e.g.:
   *
   * "source": { "pointer": "/data/attributes/email" }
   *
   * HTTP 422 Unprocessable Content (RFC 9110 §15.5.21) — the request was
   * syntactically valid JSON but semantically invalid (field constraint not met).
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public void handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    List<JsonApiError> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fe -> errorFactory.create(
            req,
            HttpStatus.UNPROCESSABLE_CONTENT,
            ApiErrorCode.VALIDATION_FAILED,
            "Validation failed",
            // getDefaultMessage() returns the annotation message, e.g. "must not be blank"
            fe.getDefaultMessage(),
            // JSON Pointer (RFC 6901): path into the request document to the offending
            // field
            JsonApiErrorSource.pointer("/data/attributes/" + fe.getField())))
        .toList();

    errorWriter.writeMany(req, res, HttpStatus.UNPROCESSABLE_CONTENT, errors);
  }

  /**
   * All domain errors (UsernameTakenException, CaptchaException, etc.).
   *
   * These extend ApiException which carries its own HttpStatus and ApiErrorCode,
   * so we just forward them. safeDetail is already sanitised at the throw site —
   * no risk of internal info leaking to the client.
   */
  @ExceptionHandler(ApiException.class)
  public void handleApiException(
      ApiException ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    errorWriter.write(
        req, res,
        ex.getStatus(),
        ex.getCode(),
        // Title: human-readable version of the error code
        titleFor(ex.getCode()),
        ex.getSafeDetail(),
        null,
        ex);
  }

  /**
   * Spring Security AuthenticationException — thrown by DaoAuthenticationProvider
   * when UserDetailsService returns null or credentials are wrong, and by other
   * Spring Security internals. The JWT filter handles ApiAuthException directly,
   * but any AuthenticationException that escapes to the controller layer is
   * caught here.
   *
   * Returns 401 with a generic message — never echo ex.getMessage() to avoid
   * leaking implementation details (e.g. "User not found").
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
        "Authentication required",
        "Invalid credentials or session expired",
        null,
        ex);
  }

  /**
   * Spring Security AccessDeniedException — thrown when a valid, authenticated
   * user attempts an action their role does not permit. The AccessDeniedHandler
   * bean handles this at filter level; this catches the rare case where it
   * surfaces at the controller layer instead.
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
        "Access denied",
        "You do not have permission to perform this action",
        null,
        ex);
  }

  /**
   * Catch-all for any exception not matched above.
   *
   * IMPORTANT: log the full exception (including stack trace) here for
   * debugging, but return only a generic message to the client. Never
   * expose internal exception messages or stack frames in the response
   * body — that would leak implementation details and aid attackers.
   */
  @ExceptionHandler(Exception.class)
  public void handleUnexpected(
      Exception ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    // @Slf4j from Lombok — log.error includes the full stack trace in the log file
    log.error("Unhandled exception at {} {}", req.getMethod(), req.getRequestURI(), ex);

    errorWriter.write(
        req, res,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ApiErrorCode.INTERNAL_SERVER_ERROR,
        "Internal server error",
        "An unexpected error occurred. Please try again later.",
        null,
        ex);
  }

  @ExceptionHandler(SchemaValidationException.class)
  public void handleValidationErrors(
      SchemaValidationException ex,
      HttpServletRequest req,
      HttpServletResponse res) throws IOException {

    var errors = ex.getErrors().stream()
        .map(errorMessage -> errorFactory.create(
            req,
            HttpStatus.UNPROCESSABLE_CONTENT,
            ApiErrorCode.VALIDATION_FAILED,
            "Schema validation failed",
            errorMessage,
            null))
        .toList();

    errorWriter.writeMany(req, res, HttpStatus.UNPROCESSABLE_CONTENT, errors);
  }

  /**
   * Converts an ApiErrorCode enum name into a readable title string.
   * e.g. USERNAME_TAKEN → "Username taken"
   */
  private String titleFor(ApiErrorCode code) {
    String raw = code.name().replace('_', ' ');
    // Capitalise first letter only
    return Character.toUpperCase(raw.charAt(0)) + raw.substring(1).toLowerCase();
  }
}
