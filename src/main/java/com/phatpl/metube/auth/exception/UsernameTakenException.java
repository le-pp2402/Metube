package com.phatpl.metube.auth.exception;

import org.springframework.http.HttpStatus;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.exception.ApiException;

public class UsernameTakenException extends ApiException {
  public UsernameTakenException() {
    super(HttpStatus.CONFLICT, ApiErrorCode.USERNAME_TAKEN, "Username is already taken");
  }
}
