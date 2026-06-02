package com.phatpl.metube.common.web;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import cn.hutool.core.util.IdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String MDC_REQUEST_ID = "requestId";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var reqId = request.getHeader(REQUEST_ID_HEADER);

    if (reqId.isBlank()) {
      reqId = IdUtil.fastSimpleUUID();
    }

    // TODO:
  }
}
