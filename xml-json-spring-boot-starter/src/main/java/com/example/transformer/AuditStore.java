package com.example.transformer;

import java.util.List;

public interface AuditStore {
    void save(AuditEntry entry);
    List<AuditEntry> page(int page, int size);
    AuditEntry get(long id);
    PageResult<AuditEntry> search(String text, int page, int size);
    int count();

    default String xmlUrl(long id) {
        return null;
    }

    default String jsonUrl(long id) {
        return null;
    }
}
