package com.example.transformer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AuditEntry implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final long id;
    private final String clientIp;
    private final long requestTime;
    private final long responseTime;
    private final boolean success;
    private final long durationMs;
    @JsonAlias("xml")
    private final byte[] xmlData;
    @JsonAlias("json")
    private final byte[] jsonData;
    private final boolean compressed;

    @JsonCreator
    public AuditEntry(
            @JsonProperty("id") long id,
            @JsonProperty("clientIp") String clientIp,
            @JsonProperty("requestTime") long requestTime,
            @JsonProperty("responseTime") long responseTime,
            @JsonProperty("success") boolean success,
            @JsonProperty("durationMs") long durationMs,
            @JsonProperty("xmlData")
            @JsonAlias("xml") byte[] xmlData,
            @JsonProperty("jsonData")
            @JsonAlias("json") byte[] jsonData,
            @JsonProperty("compressed") boolean compressed) {
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
