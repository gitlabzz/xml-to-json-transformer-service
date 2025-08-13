package com.example.transformer.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEntryRepository extends JpaRepository<JpaAuditEntry, Long> {
    Page<JpaAuditEntry> findAll(Pageable pageable);

    @Query("""
            SELECT e FROM JpaAuditEntry e
            WHERE (:q IS NULL OR :q = '' OR 
                   LOWER(e.clientIp) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(e.jsonTextExcerpt) LIKE LOWER(CONCAT('%', :q, '%')) OR
                   LOWER(e.xmlTextExcerpt) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<JpaAuditEntry> search(@Param("q") String q, Pageable pageable);
}
