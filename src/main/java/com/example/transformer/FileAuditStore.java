package com.example.transformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class FileAuditStore implements AuditStore {
    private final Path file;
    private final Deque<AuditEntry> history = new ArrayDeque<>();
    private final int maxHistory;
    private final ObjectMapper mapper = new ObjectMapper();

    public FileAuditStore(String filePath, int maxHistory) {
        this.file = Paths.get(filePath);
        this.maxHistory = maxHistory;
        load();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            byte[] data = Files.readAllBytes(file);
            try {
                List<AuditEntry> list = mapper.readValue(data, new TypeReference<List<AuditEntry>>() {});
                history.addAll(list);
                return;
            } catch (Exception ignore) {
                // not JSON - try legacy format
            }
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
                Object obj = in.readObject();
                if (obj instanceof List<?> list) {
                    for (Object e : list) {
                        if (e instanceof AuditEntry ae) {
                            history.addLast(ae);
                        }
                    }
                }
                // migrate to JSON
                persist();
            } catch (Exception ignore) {
                // ignore corrupt file
            }
        } catch (IOException ignore) {
            // ignore
        }
    }

    private void persist() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), new ArrayList<>(history));
        } catch (IOException ignore) {
            // ignore
        }
    }

    @Override
    public synchronized void save(AuditEntry entry) {
        if (history.size() >= maxHistory) {
            history.removeFirst();
        }
        history.addLast(entry);
        persist();
    }

    @Override
    public synchronized List<AuditEntry> page(int page, int size) {
        return history.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public synchronized AuditEntry get(long id) {
        for (AuditEntry e : history) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }
}
