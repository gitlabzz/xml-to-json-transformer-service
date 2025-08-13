package com.example.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final ConcurrentHashMap<String, Boolean> seen = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest r, HttpServletResponse w, FilterChain c)
      throws ServletException, IOException {
    if ("POST".equalsIgnoreCase(r.getMethod()) && r.getRequestURI().startsWith("/v1/transform")) {
      String key = r.getHeader("Idempotency-Key");
      if (key != null && !key.isBlank()) {
        if (seen.putIfAbsent(key, Boolean.TRUE) != null) {
          problem(w, 409, "Conflict", "Duplicate Idempotency-Key");
          return;
        }
      }
    }
    c.doFilter(r, w);
  }

  private void problem(HttpServletResponse w, int status, String title, String detail) throws IOException {
    var traceId = Span.current().getSpanContext().getTraceId();
    var body = MAPPER.writeValueAsString(new Problem("about:blank", title, status, detail, traceId));
    w.setStatus(status);
    w.setContentType("application/problem+json");
    w.getWriter().write(body);
  }

  record Problem(String type, String title, int status, String detail, String traceId) {}
}
