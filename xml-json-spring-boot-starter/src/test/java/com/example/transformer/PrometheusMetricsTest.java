package com.example.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(properties = {
        "audit.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@AutoConfigureMockMvc
public class PrometheusMetricsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void prometheusHasTransformMetrics() throws Exception {
        mockMvc.perform(post("/v1/transform").with(jwt())
                .contentType(MediaType.APPLICATION_XML)
                .content("<a/>") )
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/prometheus").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("transform_duration_seconds_bucket")));
    }
}
