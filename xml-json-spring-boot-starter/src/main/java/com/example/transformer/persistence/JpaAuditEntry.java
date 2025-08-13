package com.example.transformer.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "audit_entry")
public class JpaAuditEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientIp;
    private long requestTime;
    private long responseTime;
    private boolean success;
    private long durationMs;

    @Lob
    @Column(name = "xml_data", nullable = false)
    private byte[] xmlData;

    @Lob
    @Column(name = "json_data", nullable = false)
    private byte[] jsonData;

    private boolean compressed;

    public JpaAuditEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public byte[] getXmlData() {
        return xmlData;
    }

    public void setXmlData(byte[] xmlData) {
        this.xmlData = xmlData;
    }

    public byte[] getJsonData() {
        return jsonData;
    }

    public void setJsonData(byte[] jsonData) {
        this.jsonData = jsonData;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
}
