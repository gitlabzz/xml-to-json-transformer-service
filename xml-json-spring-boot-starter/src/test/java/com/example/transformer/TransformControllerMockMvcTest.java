package com.example.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.example.transformer.XmlToJsonStreamer;
import com.example.transformer.AuditService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransformController.class)
public class TransformControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private XmlToJsonStreamer xmlToJsonStreamer;

    @MockBean
    private AuditService auditService;

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
                .andExpect(status().isBadRequest());
    }
}
