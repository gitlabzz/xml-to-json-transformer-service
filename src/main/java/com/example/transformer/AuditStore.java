package com.example.transformer;

import java.util.List;

public interface AuditStore {
    void save(AuditEntry entry);
    List<AuditEntry> page(int page, int size);
    AuditEntry get(long id);
}
