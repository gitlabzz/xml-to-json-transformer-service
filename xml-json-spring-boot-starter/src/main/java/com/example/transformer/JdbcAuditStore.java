package com.example.transformer;

import com.example.transformer.persistence.AuditEntryRepository;
import com.example.transformer.persistence.JpaAuditEntry;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;

public class JdbcAuditStore implements AuditStore {
    private final AuditEntryRepository repo;
    private final boolean compress;

    public JdbcAuditStore(AuditEntryRepository repo, boolean compress) {
        this.repo = repo;
        this.compress = compress;
    }

    @Override
    public void save(AuditEntry entry) {
        try {
            String xml = entry.getXml();
            String json = entry.getJson();
            JpaAuditEntry j = new JpaAuditEntry();
            j.setClientIp(entry.getClientIp());
            j.setRequestTime(entry.getRequestTime());
            j.setResponseTime(entry.getResponseTime());
            j.setSuccess(entry.isSuccess());
            j.setDurationMs(entry.getDurationMs());
            j.setXmlData(entry.getXmlDataRaw());
            j.setJsonData(entry.getJsonDataRaw());
            j.setJsonTextExcerpt(takeExcerpt(json, 2048));
            j.setXmlTextExcerpt(takeExcerpt(xml, 2048));
            j.setCompressed(entry.isCompressed());
            repo.save(j);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AuditEntry> page(int page, int size) {
        return repo.findAll(PageRequest.of(page, size))
                .map(j -> new AuditEntry(
                        j.getId(), j.getClientIp(), j.getRequestTime(), j.getResponseTime(),
                        j.isSuccess(), j.getDurationMs(), j.getXmlData(), j.getJsonData(), j.isCompressed()))
                .getContent();
    }

    @Override
    public AuditEntry get(long id) {
        return repo.findById(id).map(j -> new AuditEntry(
                j.getId(), j.getClientIp(), j.getRequestTime(), j.getResponseTime(),
                j.isSuccess(), j.getDurationMs(), j.getXmlData(), j.getJsonData(), j.isCompressed()
        )).orElse(null);
    }

    @Override
    public PageResult<AuditEntry> search(String text, int page, int size) {
        var p = repo.search(text, PageRequest.of(page, size))
                .map(j -> new AuditEntry(
                        j.getId(), j.getClientIp(), j.getRequestTime(), j.getResponseTime(),
                        j.isSuccess(), j.getDurationMs(), j.getXmlData(), j.getJsonData(), j.isCompressed()));
        return new PageResult<>(p.getContent(), page, size, p.getTotalElements());
    }

    private static String takeExcerpt(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    @Override
    public int count() {
        return (int) repo.count();
    }
}
