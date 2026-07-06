package com.phatpl.metube.auth.exception;

import org.springframework.http.HttpStatus;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.exception.ApiException;

/**
 * Thrown when the verification token provided via POST /api/auth/verify
 * is not found in Redis — either it expired (TTL elapsed), was already
 * consumed (one-time use enforced by getAndDelete), or was never issued.
 */
public class InvalidVerificationTokenException extends ApiException {
  public InvalidVerificationTokenException() {
    super(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_VERIFICATION_TOKEN,
        "Verification token is invalid or has expired");
  }
}
