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
            byte[] xml = compress ? AuditEntry.compress(entry.getXml().getBytes()) : entry.getXml().getBytes();
            byte[] json = compress ? AuditEntry.compress(entry.getJson().getBytes()) : entry.getJson().getBytes();
            JpaAuditEntry j = new JpaAuditEntry();
            j.setClientIp(entry.getClientIp());
            j.setRequestTime(entry.getRequestTime());
            j.setResponseTime(entry.getResponseTime());
            j.setSuccess(entry.isSuccess());
            j.setDurationMs(entry.getDurationMs());
            j.setXmlData(xml);
            j.setJsonData(json);
            j.setCompressed(compress);
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
    public List<AuditEntry> search(String text) {
        return page(0, 50);
    }

    @Override
    public int count() {
        return (int) repo.count();
    }
}
