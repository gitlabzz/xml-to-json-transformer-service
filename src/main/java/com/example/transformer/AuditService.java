package com.example.transformer;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuditService {
    private final AuditStore store;
    private final boolean compress;
    private final AtomicLong counter = new AtomicLong();

    public AuditService(AuditProperties props, AuditStore store) {
        this.store = store;
        this.compress = props.isCompress();
    }

    public void add(String clientIp, long start, long end, boolean success, byte[] xml, byte[] json) {
        try {
            byte[] x = compress ? AuditEntry.compress(xml) : xml;
            byte[] j = compress ? AuditEntry.compress(json) : json;
            AuditEntry entry = new AuditEntry(counter.incrementAndGet(), clientIp, start, end,
                    success, end - start, x, j, compress);
            store.save(entry);
        } catch (IOException e) {
            // ignore
        }
    }

    public List<AuditEntry> page(int page, int size) {
        return store.page(page, size);
    }

    public AuditEntry get(long id) {
        return store.get(id);
    }
}
