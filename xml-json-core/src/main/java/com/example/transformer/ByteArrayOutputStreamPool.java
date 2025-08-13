package com.example.transformer;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Simple pool for reusing {@link ByteArrayOutputStream} instances to reduce
 * temporary allocations during transformation.
 */
public class ByteArrayOutputStreamPool {
    private final BlockingQueue<ByteArrayOutputStream> queue;

    public ByteArrayOutputStreamPool() {
        this(128);
    }

    public ByteArrayOutputStreamPool(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /** Borrow a buffer from the pool or create a new one if empty. */
    public ByteArrayOutputStream borrow() {
        ByteArrayOutputStream b = queue.poll();
        return b != null ? b : new ByteArrayOutputStream(8192);
    }

    /**
     * Return a buffer to the pool after resetting it. Buffers that cannot be
     * offered back due to pool saturation are simply discarded.
     */
    public void release(ByteArrayOutputStream b) {
        if (b == null) return;
        b.reset();
        queue.offer(b);
    }
}
