package com.example.transformer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ContentGuardFilter implements Filter {
  private static final long MAX = 10L * 1024 * 1024; // 10 MiB

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;
    if ("/v1/transform".equals(r.getRequestURI())) {
      String ct = r.getContentType();
      if (ct == null || !(ct.contains("application/xml") || ct.contains("text/xml"))) {
        w.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
        return;
      }
      long len = r.getContentLengthLong();
      if (len > MAX) {
        w.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Payload Too Large");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
