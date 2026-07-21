package com.phatpl.metube.common.api;

/**
 * Field-level error detail — one entry per constraint violation.
 * Mainly used in 422 Unprocessable Content responses.
 *
 * Example:
 * { "field": "email", "message": "must be a valid email address" }
 */
public record ErrorDetail(String field, String message) {
}
