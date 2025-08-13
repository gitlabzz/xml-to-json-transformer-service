package com.example.transformer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Profile("apikey")
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
  private final String apiKey;

  public ApiKeyFilter(@Value("${API_KEY:}") String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String key = request.getHeader("X-API-Key");
    if (apiKey != null && !apiKey.isBlank() && apiKey.equals(key)) {
      var auth = new UsernamePasswordAuthenticationToken("api-key", key, List.of());
      SecurityContextHolder.getContext().setAuthentication(auth);
      filterChain.doFilter(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
    }
  }
}
