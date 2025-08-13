package com.example.transformer;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class TraceHeaderAdvice {
  @ModelAttribute
  public void addTraceHeader(HttpServletResponse res) {
    String traceId = Span.current().getSpanContext().getTraceId();
    if (traceId != null && !traceId.isEmpty()) res.setHeader("X-Trace-Id", traceId);
  }
}
