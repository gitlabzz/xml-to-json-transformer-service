package com.example.transformer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/actuator/health/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
        .requestMatchers("/v1/transform", "/audit/**").authenticated()
        .anyRequest().authenticated()
      )
      .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
      .headers(h -> h
        .xssProtection(x -> x.block(true))
        .contentSecurityPolicy(c -> c.policyDirectives("default-src 'self'"))
        .frameOptions(f -> f.sameOrigin())
      );
    return http.build();
  }
}
