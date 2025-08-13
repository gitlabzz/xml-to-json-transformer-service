package com.example.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
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
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse w = (HttpServletResponse) res;
    if ("/v1/transform".equals(r.getRequestURI())) {
      String ct = r.getContentType();
      if (ct == null || !(ct.contains("application/xml") || ct.contains("text/xml"))) {
        problem(w, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
        return;
      }
      long len = r.getContentLengthLong();
      if (len > MAX) {
        problem(w, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Payload Too Large");
        return;
      }
    }
    chain.doFilter(req, res);
  }

  private void problem(HttpServletResponse w, int status, String title) throws IOException {
    String traceId = Span.current().getSpanContext().getTraceId();
    var body = MAPPER.writeValueAsString(new Problem("about:blank", title, status, title, traceId));
    w.setStatus(status);
    w.setContentType("application/problem+json");
    w.getWriter().write(body);
  }

  record Problem(String type, String title, int status, String detail, String traceId) {}
}
