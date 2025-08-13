package com.example.transformer;

import com.example.transformer.persistence.AuditEntryRepository;
import com.example.transformer.persistence.JpaAuditEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AuditEntryRepositoryTest {

    @Autowired
    private AuditEntryRepository repo;

    @Test
    void searchByExcerpt() {
        JpaAuditEntry e = new JpaAuditEntry();
        e.setClientIp("1.1.1.1");
        e.setRequestTime(1L);
        e.setResponseTime(2L);
        e.setSuccess(true);
        e.setDurationMs(1L);
        e.setXmlData(new byte[0]);
        e.setJsonData(new byte[0]);
        e.setXmlTextExcerpt("hello world");
        e.setJsonTextExcerpt("json content");
        e.setCompressed(false);
        repo.save(e);

        var page = repo.search("hello", PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
