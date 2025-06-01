package com.example.transformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileAuditStoreTest {

    @Test
    public void writesAndReadsJson() throws Exception {
        Path file = Files.createTempFile("audit", ".json");
        Files.deleteIfExists(file);
        FileAuditStore store = new FileAuditStore(file.toString(), 10);
        AuditEntry e = new AuditEntry(1L, "ip", 1L, 2L, true, 1L, new byte[]{1}, new byte[]{2}, false);
        store.save(e);

        ObjectMapper mapper = new ObjectMapper();
        List<AuditEntry> list = mapper.readValue(file.toFile(), new TypeReference<List<AuditEntry>>() {});
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());

        Files.deleteIfExists(file);
    }

    @Test
    public void migratesFromLegacyFormat() throws Exception {
        Path file = Files.createTempFile("audit", ".ser");
        AuditEntry e = new AuditEntry(1L, "ip", 1L, 2L, true, 1L, new byte[]{1}, new byte[]{2}, false);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(List.of(e));
        }
        FileAuditStore store = new FileAuditStore(file.toString(), 10);
        assertEquals(1, store.page(0, 10).size());

        ObjectMapper mapper = new ObjectMapper();
        List<AuditEntry> list = mapper.readValue(file.toFile(), new TypeReference<List<AuditEntry>>() {});
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());

        Files.deleteIfExists(file);
    }
}
