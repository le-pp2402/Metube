package com.phatpl.metube.common.web;

import java.io.IOException;
import java.util.regex.Pattern;

import org.slf4j.MDC;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.phatpl.metube.common.logging.LogMdcKeys;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  private static final Pattern SAFE_REQUEST_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9._:\\-]+$");
  private static final int MAX_REQUEST_ID_LENGTH = 40;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    var reqId = resolveRequestId(request);

    MDC.put(LogMdcKeys.REQUEST_ID, reqId);

    try {
      response.setHeader(REQUEST_ID_HEADER, reqId);
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(LogMdcKeys.REQUEST_ID);
    }
  }

  private String resolveRequestId(HttpServletRequest request) {
    var reqId = request.getHeader(REQUEST_ID_HEADER);

    if (isValidRequestId(reqId)) {
      return reqId;
    }

    return IdUtil.fastSimpleUUID();
  }

  private boolean isValidRequestId(String reqId) {
    if (StrUtil.isBlank(reqId)) {
      return false;
    }

    if (reqId.length() > MAX_REQUEST_ID_LENGTH) {
      return false;
    }

    return SAFE_REQUEST_ID_PATTERN.matcher(reqId).matches();
  }
}
