package com.phatpl.metube.common.api;

import java.util.List;

/**
 * Error body — always present when {@code ok = false}.
 *
 * <pre>
 * {
 *   "code":    "VALIDATION_FAILED",
 *   "message": "Request validation failed",
 *   "details": [ { "field": "email", "message": "must be a valid email" } ]
 * }
 * </pre>
 */
public record ApiErrorBody(
    String code,
    String message,
    List<ErrorDetail> details
) {
  /** Convenience factory — no field-level details */
  public static ApiErrorBody of(ApiErrorCode code, String message) {
    return new ApiErrorBody(code.name(), message, List.of());
  }

  /** Convenience factory — with field-level details */
  public static ApiErrorBody of(ApiErrorCode code, String message, List<ErrorDetail> details) {
    return new ApiErrorBody(code.name(), message, details);
  }
}
