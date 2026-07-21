package com.phatpl.metube.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Top-level response envelope for every API call.
 *
 * <p>Success shape:
 * <pre>
 * { "ok": true,  "data": {...}, "error": null, "meta": null }
 * </pre>
 *
 * <p>Error shape:
 * <pre>
 * { "ok": false, "data": null,  "error": { "code": "...", "message": "...", "details": [] }, "meta": {...} }
 * </pre>
 *
 * @param <T> type of the {@code data} payload
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(
    boolean ok,
    T data,
    ApiErrorBody error,
    Object meta
) {
  /** Successful response with a data payload */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data, null, null);
  }

  /** Successful response with no data (e.g. register, delete) */
  public static ApiResponse<Void> success() {
    return new ApiResponse<>(true, null, null, null);
  }

  /** Error response — carries an ApiErrorBody, no data */
  public static <T> ApiResponse<T> failure(ApiErrorBody errorBody, Object meta) {
    return new ApiResponse<>(false, null, errorBody, meta);
  }

  /** Error response without meta */
  public static <T> ApiResponse<T> failure(ApiErrorBody errorBody) {
    return new ApiResponse<>(false, null, errorBody, null);
  }
}
