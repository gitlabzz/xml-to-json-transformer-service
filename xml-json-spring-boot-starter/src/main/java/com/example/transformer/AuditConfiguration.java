package com.example.transformer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfiguration {
    @Bean
    public AuditStore auditStore(AuditProperties props) {
        if ("file".equalsIgnoreCase(props.getBackend())) {
            return new FileAuditStore(props.getFilePath(), props.getHistorySize());
        }
        return new InMemoryAuditStore(props.getHistorySize());
    }

    @Bean
    public AuditService auditService(AuditProperties props) {
        return new AuditService(props);
    }
}
