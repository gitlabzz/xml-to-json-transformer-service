package com.example.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransformControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void validXml() throws Exception {
        mockMvc.perform(post("/transform")
                .contentType(MediaType.APPLICATION_XML)
                .content("<a/>"))
                .andExpect(status().isOk());
    }

    @Test
    public void malformedXml() throws Exception {
        mockMvc.perform(post("/transform")
                .contentType(MediaType.APPLICATION_XML)
                .content("<a>"))
                .andExpect(status().is5xxServerError());
    }
}
