package com.example.transformer;

import io.opentelemetry.api.trace.Span;
import jakarta.xml.stream.XMLStreamException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProblemHandler {
  public record Problem(String type, String title, int status, String detail, String traceId) {}

  @ExceptionHandler(XMLStreamException.class)
  public ResponseEntity<Problem> xml(XMLStreamException ex) {
    return problem(400, "Bad Request", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> any(Exception ex) {
    return problem(500, "Internal Server Error", ex.getMessage());
  }

  private ResponseEntity<Problem> problem(int status, String title, String detail) {
    String traceId = Span.current().getSpanContext().getTraceId();
    var p = new Problem("about:blank", title, status, detail, traceId);
    return ResponseEntity.status(status)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(p);
  }
}
