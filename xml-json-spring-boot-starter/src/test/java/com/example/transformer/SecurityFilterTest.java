package com.example.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(properties = {
        "audit.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@AutoConfigureMockMvc
class SecurityFilterTest {

  @Autowired
  MockMvc mockMvc;

  @Test
  void unauthorizedWithoutToken() throws Exception {
    mockMvc.perform(post("/v1/transform")
            .contentType(MediaType.APPLICATION_XML)
            .content("<a/>") )
        .andExpect(status().isUnauthorized());
  }

  @Test
  void unsupportedContentType() throws Exception {
    mockMvc.perform(post("/v1/transform").with(jwt())
            .contentType(MediaType.TEXT_PLAIN)
            .content("abc"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Unsupported Media Type"));
  }

  @Test
  void payloadTooLarge() throws Exception {
    byte[] huge = new byte[(int)(10 * 1024 * 1024 + 1)];
    mockMvc.perform(post("/v1/transform").with(jwt())
            .contentType(MediaType.APPLICATION_XML)
            .content(huge))
        .andExpect(status().isPayloadTooLarge())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Payload Too Large"));
  }

  @Test
  void rateLimitExceeded() throws Exception {
    for (int i = 0; i < 60; i++) {
      mockMvc.perform(post("/v1/transform").with(jwt())
              .contentType(MediaType.APPLICATION_XML)
              .content("<a/>") )
          .andExpect(status().isOk());
    }
    mockMvc.perform(post("/v1/transform").with(jwt())
            .contentType(MediaType.APPLICATION_XML)
            .content("<a/>") )
        .andExpect(status().isTooManyRequests())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Too Many Requests"));
  }

  @Test
  void idempotencyConflict() throws Exception {
    mockMvc.perform(post("/v1/transform").with(jwt())
            .contentType(MediaType.APPLICATION_XML)
            .content("<a/>")
            .header("Idempotency-Key", "abc"))
        .andExpect(status().isOk());

    mockMvc.perform(post("/v1/transform").with(jwt())
            .contentType(MediaType.APPLICATION_XML)
            .content("<a/>")
            .header("Idempotency-Key", "abc"))
        .andExpect(status().isConflict())
        .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
        .andExpect(jsonPath("$.title").value("Conflict"));
  }
}
