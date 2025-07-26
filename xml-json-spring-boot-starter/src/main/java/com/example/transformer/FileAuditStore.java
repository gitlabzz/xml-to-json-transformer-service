package com.example.transformer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileAuditStore implements AuditStore {
    private static final Logger logger = LoggerFactory.getLogger(FileAuditStore.class);
    private final Path file;
    private final Deque<AuditEntry> history = new ConcurrentLinkedDeque<>();
    private final int maxHistory;
    private final ObjectMapper mapper;

    public FileAuditStore(String filePath, int maxHistory) {
        this.file = Paths.get(filePath);
        this.maxHistory = maxHistory;
        JsonFactory factory = JsonFactory.builder()
                .disable(JsonWriteFeature.ESCAPE_NON_ASCII)
                .build();
        this.mapper = JsonMapper.builder(factory).build();
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
            } catch (Exception e) {
                logger.warn("Failed to load legacy audit file {}", file, e);
            }
        } catch (IOException e) {
            logger.warn("Failed to load audit file {}", file, e);
        }
    }

    private void persist() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), new ArrayList<>(history));
        } catch (IOException e) {
            logger.warn("Failed to persist audit file {}", file, e);
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
    public List<AuditEntry> page(int page, int size) {
        return history.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public AuditEntry get(long id) {
        for (AuditEntry e : history) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    @Override
    public synchronized List<AuditEntry> search(String text) {
        String lower = text.toLowerCase();
        return history.stream()
                .filter(e -> containsIgnoreCase(e, lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean containsIgnoreCase(AuditEntry e, String lower) {
        try {
            return e.getXml().toLowerCase().contains(lower)
                    || e.getJson().toLowerCase().contains(lower);
        } catch (IOException ex) {
            return false;
        }
    }
}
