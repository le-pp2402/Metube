package com.phatpl.metube.auth.exception;

import org.springframework.http.HttpStatus;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.exception.ApiException;

/**
 * Thrown on login when the user's email has not yet been verified.
 * Checked after password is confirmed so this does not leak whether
 * a valid account exists under that username.
 */
public class EmailNotVerifiedException extends ApiException {
  public EmailNotVerifiedException() {
    super(HttpStatus.FORBIDDEN, ApiErrorCode.EMAIL_NOT_VERIFIED,
        "Please verify your email address before logging in");
  }
}
