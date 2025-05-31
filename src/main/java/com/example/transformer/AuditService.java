package com.example.transformer;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AuditService {
    private final Deque<AuditEntry> history = new ArrayDeque<>();
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
        } catch (IOException e) {
            // ignore
        }
    }

    public List<AuditEntry> page(int page, int size) {
        synchronized (history) {
            return history.stream()
                    .skip((long) page * size)
                    .limit(size)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public AuditEntry get(long id) {
        synchronized (history) {
            for (AuditEntry e : history) {
                if (e.getId() == id) {
                    return e;
                }
            }
        }
        return null;
    }
}
