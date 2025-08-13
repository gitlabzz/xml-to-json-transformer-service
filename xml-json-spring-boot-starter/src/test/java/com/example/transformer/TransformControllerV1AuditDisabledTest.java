package com.example.transformer;

import com.example.transformer.api.v1.TransformControllerV1;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@WebMvcTest(controllers = TransformControllerV1.class)
public class TransformControllerV1AuditDisabledTest {

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
        when(auditProperties.isEnabled()).thenReturn(false);
        when(metrics.start()).thenReturn(Timer.start(new SimpleMeterRegistry()));
    }

    @Test
    void noAuditWhenDisabled() throws Exception {
        mockMvc.perform(post("/v1/transform")
                .contentType(MediaType.APPLICATION_XML)
                .content("<a/>") )
                .andExpect(status().isOk());

        verify(auditService, never()).add(any(), anyLong(), anyLong(), anyBoolean(), any(), any());
    }
}
