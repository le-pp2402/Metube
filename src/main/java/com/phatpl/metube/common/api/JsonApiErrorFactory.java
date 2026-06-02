package com.phatpl.metube.common.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.IdUtil;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JsonApiErrorFactory {
  public JsonApiError create(
      HttpServletRequest req,
      HttpStatus status,
      ApiErrorCode code,
      String title,
      String detail,
      JsonApiErrorSource source) {
    return new JsonApiError(
        IdUtil.fastUUID(),
        String.valueOf(status.value()),
        code.name(),
        title,
        detail,
        source,
        Map.of("path", req.getRequestURI()));
  }

  public JsonApiErrorDocument document(JsonApiError error) {
    return JsonApiErrorDocument.of(error);
  }
}
