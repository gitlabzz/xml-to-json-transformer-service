package com.example.transformer;

import com.example.transformer.persistence.AuditEntryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditStoreConfig {
    @Bean
    @ConditionalOnProperty(name = "audit.backend", havingValue = "memory", matchIfMissing = true)
    public AuditStore memoryStore(AuditProperties props) {
        return new InMemoryAuditStore(props.getHistorySize());
    }

    @Bean
    @ConditionalOnProperty(name = "audit.backend", havingValue = "file")
    public AuditStore fileStore(AuditProperties props) {
        return new FileAuditStore(props.getFilePath(), props.getHistorySize());
    }

    @Bean
    @ConditionalOnProperty(name = "audit.backend", havingValue = "jdbc")
    public AuditStore jdbcStore(AuditEntryRepository repo, AuditProperties props) {
        return new JdbcAuditStore(repo, props.isCompress());
    }
}
