package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuditServiceConcurrencyTest {

    @Test
    public void concurrentAdd() throws Exception {
        AuditProperties props = new AuditProperties();
        props.setHistorySize(1000);
        props.setCompress(false);
        AuditService service = new AuditService(props);

        int threads = 10;
        int perThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads * perThread);
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < perThread; j++) {
                    service.add("ip", 0L, 1L, true, new byte[0], new byte[0]);
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        assertEquals(threads * perThread, service.page(0, threads * perThread).size());
    }
}
