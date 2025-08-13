package com.example.transformer;

import com.example.transformer.api.v1.TransformControllerV1;
import com.example.transformer.AuditProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.hamcrest.Matchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(controllers = TransformControllerV1.class)
@Import(CorrelationFilter.class)
public class TransformControllerV1MockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private XmlToJsonStreamer xmlToJsonStreamer;

    @MockBean
    private AuditService auditService;

    @MockBean
    private AuditProperties auditProperties;

    @MockBean
    private TransformMetrics metrics;

    @BeforeEach
    void setup() {
        when(auditProperties.isEnabled()).thenReturn(true);
        when(metrics.start()).thenReturn(Timer.start(new SimpleMeterRegistry()));
    }

    @Test
    public void validXml() throws Exception {
        mockMvc.perform(post("/v1/transform").with(jwt())
                .contentType(MediaType.APPLICATION_XML)
                .content("<a/>") )
                .andExpect(status().isOk());
    }

    @Test
    public void headersPresent() throws Exception {
        mockMvc.perform(post("/v1/transform").with(jwt())
                .contentType(MediaType.APPLICATION_XML)
                .content("<a/>")
                .header(CorrelationFilter.CORRELATION, "abc"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationFilter.CORRELATION, "abc"))
                .andExpect(header().string("X-Trace-Id", Matchers.not(Matchers.isEmptyString())));
    }

    @Test
    public void malformedXml() throws Exception {
        doThrow(new XMLStreamException("invalid"))
                .when(xmlToJsonStreamer).transform(any(InputStream.class), any(OutputStream.class));

        mockMvc.perform(post("/v1/transform").with(jwt())
                .contentType(MediaType.APPLICATION_XML)
                .content("<a>") )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }
}
