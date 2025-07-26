package com.example.transformer;

public record AuditEntrySummary(long id, String clientIp, long requestTime, boolean success) {
    public AuditEntrySummary(AuditEntry entry) {
        this(entry.getId(), entry.getClientIp(), entry.getRequestTime(), entry.isSuccess());
    }
}

