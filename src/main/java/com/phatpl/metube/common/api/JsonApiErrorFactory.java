package com.phatpl.metube.common.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.phatpl.metube.common.logging.LogMdcKeys;

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

    var meta = new LinkedHashMap<String, Object>();

    meta.put("request_id", MDC.get(LogMdcKeys.REQUEST_ID));
    meta.put("path", req.getRequestURI());
    meta.put("method", req.getMethod());

    return new JsonApiError(
        IdUtil.fastUUID(),
        String.valueOf(status.value()),
        code.name(),
        title,
        detail,
        source,
        meta
      );
  }

  public JsonApiErrorDocument document(JsonApiError error) {
    return JsonApiErrorDocument.of(error);
  }
}
