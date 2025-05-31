package com.example.transformer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuditServiceTest {

    @Test
    public void addAndRetrieve() throws Exception {
        AuditProperties props = new AuditProperties();
        props.setCompress(false);
        InMemoryAuditStore store = new InMemoryAuditStore(10);
        AuditService service = new AuditService(props, store);

        service.add("127.0.0.1", 0L, 1L, true, "<a/>".getBytes(), "{}".getBytes());

        assertEquals(1, service.page(0, 10).size());
        AuditEntry entry = service.get(1);
        assertNotNull(entry);
        assertEquals("127.0.0.1", entry.getClientIp());
    }
}
