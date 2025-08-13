package com.example.transformer;

import com.example.transformer.api.v1.AuditControllerV1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(controllers = AuditControllerV1.class)
@Import(CorrelationFilter.class)
public class AuditSearchApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    void pagedSearch() throws Exception {
        AuditEntry e = new AuditEntry(1L, "1.2.3.4", 0L, 0L, true, 0L, new byte[0], new byte[0], false);
        PageResult<AuditEntry> result = new PageResult<>(List.of(e), 0, 5, 1);
        when(auditService.search("x", 0, 5)).thenReturn(result);

        mockMvc.perform(get("/v1/audit/search").param("q", "x").param("page", "0").param("size", "5").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.total").value(1));
    }
}
