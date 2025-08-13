package com.example.transformer;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI api() {
    return new OpenAPI()
      .addServersItem(new Server().url("/"))
      .info(new Info()
        .title("XML to JSON Transformer")
        .version("1.0.0")
        .description("Versioned API. Use /v1/transform"));
  }
}
