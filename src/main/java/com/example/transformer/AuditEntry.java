package com.example.transformer;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AuditEntry {
    private final long id;
    private final String clientIp;
    private final long requestTime;
    private final long responseTime;
    private final boolean success;
    private final long durationMs;
    private final byte[] xmlData;
    private final byte[] jsonData;
    private final boolean compressed;

    public AuditEntry(long id, String clientIp, long requestTime, long responseTime, boolean success,
                      long durationMs, byte[] xmlData, byte[] jsonData, boolean compressed) {
        this.id = id;
        this.clientIp = clientIp;
        this.requestTime = requestTime;
        this.responseTime = responseTime;
        this.success = success;
        this.durationMs = durationMs;
        this.xmlData = xmlData;
        this.jsonData = jsonData;
        this.compressed = compressed;
    }

    public long getId() {
        return id;
    }

    public String getClientIp() {
        return clientIp;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getXml() throws IOException {
        return new String(decompress(xmlData), java.nio.charset.StandardCharsets.UTF_8);
    }

    public String getJson() throws IOException {
        return new String(decompress(jsonData), java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] decompress(byte[] data) throws IOException {
        if (!compressed) {
            return data;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             GZIPInputStream gis = new GZIPInputStream(bis)) {
            return gis.readAllBytes();
        }
    }

    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }
}
