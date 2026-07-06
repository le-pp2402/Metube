package com.phatpl.metube.auth.exception;

import org.springframework.http.HttpStatus;

import com.phatpl.metube.common.api.ApiErrorCode;
import com.phatpl.metube.common.exception.ApiException;

public class EmailTakenException extends ApiException {
  public EmailTakenException() {
    super(HttpStatus.CONFLICT, ApiErrorCode.EMAIL_TAKEN, "Email is already registered");
  }
}
