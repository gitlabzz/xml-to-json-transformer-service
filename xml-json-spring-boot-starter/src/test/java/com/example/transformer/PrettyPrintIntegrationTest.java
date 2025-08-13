package com.example.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(properties = {
        "mapping.pretty-print=true",
        "audit.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
@AutoConfigureMockMvc
public class PrettyPrintIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void prettyPrintEnabled() throws Exception {
        mockMvc.perform(post("/v1/transform").with(jwt())
                .contentType(MediaType.APPLICATION_XML)
                .content("<root><a>1</a></root>") )
                .andExpect(status().isOk())
                .andExpect(content().string("{\n  \"root\" : {\n    \"a\" : 1\n  }\n}"));
    }
}
