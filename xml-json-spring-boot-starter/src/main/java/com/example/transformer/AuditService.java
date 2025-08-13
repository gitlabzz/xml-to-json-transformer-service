package com.example.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private final AuditStore store;
    private final boolean compress;
    private final AtomicLong counter = new AtomicLong();

    public AuditService(AuditStore store, AuditProperties props) {
        this.store = store;
        this.compress = props.isCompress();
    }

    public void add(String clientIp, long start, long end, boolean success, byte[] xml, byte[] json) {
        try {
            byte[] x = compress ? AuditEntry.compress(xml) : xml;
            byte[] j = compress ? AuditEntry.compress(json) : json;
            long id = counter.incrementAndGet();
            AuditEntry entry = new AuditEntry(id, clientIp, start, end, success, end - start, x, j, compress);
            store.save(entry);
            logger.info("Audit entry {} stored for {} - success: {}", entry.getId(), clientIp, success);
        } catch (IOException e) {
            logger.error("Failed to store audit entry for {}", clientIp, e);
        }
    }

    public List<AuditEntry> page(int page, int size) {
        return store.page(page, size);
    }

    public AuditEntry get(long id) {
        return store.get(id);
    }

    public int count() {
        return store.count();
    }

    public List<AuditEntry> search(String text) {
        return store.search(text);
    }

    public void clear() {
        counter.set(0);
    }
}
