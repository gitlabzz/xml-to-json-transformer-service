package com.example.transformer.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryRepository extends JpaRepository<JpaAuditEntry, Long> {
    Page<JpaAuditEntry> findAll(Pageable pageable);
}
