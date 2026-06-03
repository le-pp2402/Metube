package com.phatpl.metube.auth.security;

import org.springframework.security.core.AuthenticationException;

import com.phatpl.metube.common.api.ApiErrorCode;

public class ApiAuthenticationException extends AuthenticationException {
  private final ApiErrorCode code;
  private final String safeDetail;

  public ApiAuthenticationException(ApiErrorCode code, String safeDetail) {
    super(safeDetail);
    this.code = code;
    this.safeDetail = safeDetail;
  }

  public ApiAuthenticationException(
      ApiErrorCode code,
      String safeDetail,
      Throwable cause) {
    super(safeDetail, cause);
    this.code = code;
    this.safeDetail = safeDetail;
  }

  public ApiErrorCode getCode() {
    return code;
  }

  public String getSafeDetail() {
    return safeDetail;
  }
}