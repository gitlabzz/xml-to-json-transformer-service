package com.example.transformer;

import java.util.List;

public interface AuditStore {
    void save(AuditEntry entry);
    List<AuditEntry> page(int page, int size);
    AuditEntry get(long id);
    /**
     * Returns all audit entries whose XML or JSON payload contains the given text.
     */
    List<AuditEntry> search(String text);
}
