package com.example.transformer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private final Map<String, io.github.bucket4j.Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest r, HttpServletResponse w, FilterChain c) throws ServletException, IOException {
    String key = Optional.ofNullable(r.getHeader("X-API-Key")).orElse(r.getRemoteAddr());
    var conf = io.github.bucket4j.Bandwidth.classic(60, io.github.bucket4j.Refill.greedy(60, java.time.Duration.ofMinutes(1)));
    var bucket = buckets.computeIfAbsent(key, k -> io.github.bucket4j.Bucket4j.builder().addLimit(conf).build());
    if (bucket.tryConsume(1)) {
      c.doFilter(r, w);
    } else {
      w.setStatus(429);
      w.getWriter().write("Rate limit exceeded");
    }
  }
}
