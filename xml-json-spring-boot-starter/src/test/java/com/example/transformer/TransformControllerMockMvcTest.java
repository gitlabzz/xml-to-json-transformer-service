package com.example.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.example.transformer.XmlToJsonStreamer;
import com.example.transformer.AuditService;
import com.example.transformer.AuditProperties;
import org.junit.jupiter.api.BeforeEach;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = TransformController.class)
public class TransformControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private XmlToJsonStreamer xmlToJsonStreamer;

    @MockBean
    private AuditService auditService;

    @MockBean
    private AuditProperties auditProperties;

    @BeforeEach
    void setup() {
        when(auditProperties.isEnabled()).thenReturn(true);
    }

    @Test
    public void validXml() throws Exception {
        mockMvc.perform(post("/transform")
                .contentType(MediaType.APPLICATION_XML)
                .content("<a/>"))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("Link", "</v1/transform>; rel=\"successor-version\""));
    }

    @Test
    public void malformedXml() throws Exception {
        doThrow(new XMLStreamException("invalid"))
                .when(xmlToJsonStreamer).transform(any(InputStream.class), any(OutputStream.class));

        mockMvc.perform(post("/transform")
                .contentType(MediaType.APPLICATION_XML)
                .content("<a>"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("Link", "</v1/transform>; rel=\"successor-version\""));
    }
}
