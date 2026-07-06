package com.phatpl.metube.common.exception;

import org.springframework.http.HttpStatus;

import com.phatpl.metube.common.api.ApiErrorCode;

public class ApiException extends RuntimeException {
  private final HttpStatus status;
  private final ApiErrorCode code;
  private final String safeDetail;

  public ApiException(HttpStatus status, ApiErrorCode code, String safeDetail) {
    super(safeDetail);
    this.status = status;
    this.code = code;
    this.safeDetail = safeDetail;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public ApiErrorCode getCode() {
    return code;
  }

  public String getSafeDetail() {
    return safeDetail;
  }
}