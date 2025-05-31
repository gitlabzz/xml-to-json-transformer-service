package com.example.transformer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class FileAuditStore implements AuditStore {
    private final Path file;
    private final Deque<AuditEntry> history = new ConcurrentLinkedDeque<>();
    private final int maxHistory;

    public FileAuditStore(String filePath, int maxHistory) {
        this.file = Paths.get(filePath);
        this.maxHistory = maxHistory;
        load();
    }

    private void load() {
        if (Files.exists(file)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
                Object obj = in.readObject();
                if (obj instanceof List<?> list) {
                    for (Object e : list) {
                        if (e instanceof AuditEntry ae) {
                            history.addLast(ae);
                        }
                    }
                }
            } catch (Exception e) {
                // ignore corrupt file
            }
        }
    }

    private void persist() {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(new ArrayList<>(history));
        } catch (IOException e) {
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
}
