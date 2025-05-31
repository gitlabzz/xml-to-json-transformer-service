package com.example.transformer;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private final Deque<AuditEntry> history = new ConcurrentLinkedDeque<>();
    private final int maxHistory;
    private final boolean compress;
    private final AtomicLong counter = new AtomicLong();

    public AuditService(AuditProperties props) {
        this.maxHistory = props.getHistorySize();
        this.compress = props.isCompress();
    }

    public void add(String clientIp, long start, long end, boolean success, byte[] xml, byte[] json) {
        try {
            byte[] x = compress ? AuditEntry.compress(xml) : xml;
            byte[] j = compress ? AuditEntry.compress(json) : json;
            AuditEntry entry = new AuditEntry(counter.incrementAndGet(), clientIp, start, end,
                    success, end - start, x, j, compress);
            synchronized (history) {
                if (history.size() >= maxHistory) {
                    history.removeFirst();
                }
                history.addLast(entry);
            }
            logger.info("Audit entry {} stored for {} - success: {}", entry.getId(), clientIp, success);
        } catch (IOException e) {
            logger.error("Failed to store audit entry for {}", clientIp, e);
        }
    }

    public List<AuditEntry> page(int page, int size) {
        return history.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public AuditEntry get(long id) {
        for (AuditEntry e : history) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    /**
     * Clears all stored audit entries. Used in tests.
     */
    public void clear() {
        synchronized (history) {
            history.clear();
            counter.set(0);
        }
    }
}
