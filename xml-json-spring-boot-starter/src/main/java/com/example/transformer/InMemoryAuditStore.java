package com.example.transformer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class InMemoryAuditStore implements AuditStore {
    private final Deque<AuditEntry> history = new ConcurrentLinkedDeque<>();
    private final int maxHistory;

    public InMemoryAuditStore(int maxHistory) {
        this.maxHistory = maxHistory;
    }

    @Override
    public void save(AuditEntry entry) {
        if (history.size() >= maxHistory) {
            history.removeFirst();
        }
        history.addLast(entry);
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
    public List<AuditEntry> search(String text) {
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
