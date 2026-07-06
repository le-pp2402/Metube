package com.phatpl.metube.auth.exception;

import org.springframework.http.HttpStatus;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.exception.ApiException;

/**
 * Thrown on login when the user's account is not active or has been deleted.
 * Checked AFTER the password is verified to avoid leaking whether a
 * disabled account exists under that username.
 */
public class AccountDisabledException extends ApiException {
  public AccountDisabledException() {
    super(HttpStatus.FORBIDDEN, ApiErrorCode.ACCOUNT_DISABLED, "Account is disabled");
  }
}
