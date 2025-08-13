package com.example.transformer;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationFilter implements Filter {
  public static final String CORRELATION = "X-Request-ID";
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;
    String id = r.getHeader(CORRELATION);
    if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
    MDC.put("correlationId", id);
    try {
      chain.doFilter(req, res);
    } finally {
      w.setHeader(CORRELATION, id);
      MDC.clear();
    }
  }
}
