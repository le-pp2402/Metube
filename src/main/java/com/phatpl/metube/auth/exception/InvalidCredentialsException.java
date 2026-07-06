package com.phatpl.metube.auth.exception;

import org.springframework.http.HttpStatus;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.exception.ApiException;

/**
 * Thrown on login when either the username does not exist or the password
 * does not match. Using a single, intentionally vague message prevents
 * username enumeration attacks (attacker cannot distinguish "no such user"
 * from "wrong password").
 */
public class InvalidCredentialsException extends ApiException {
  public InvalidCredentialsException() {
    super(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS,
        "Invalid username or password");
  }
}
