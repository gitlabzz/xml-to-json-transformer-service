package com.example.transformer;

import com.example.transformer.persistence.AuditEntryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

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

    @Bean
    @ConditionalOnProperty(name = "audit.backend", havingValue = "s3")
    public S3Client s3Client(AuditProperties p) {
        S3ClientBuilder b = S3Client.builder();
        if (p.getS3Endpoint() != null) {
            b.endpointOverride(URI.create(p.getS3Endpoint()));
        }
        if (p.getS3Region() != null) {
            b.region(Region.of(p.getS3Region()));
        }
        return b.build();
    }

    @Bean
    @ConditionalOnProperty(name = "audit.backend", havingValue = "s3")
    public AuditStore s3Store(S3Client client, AuditEntryRepository repo, AuditProperties props) {
        return new S3AuditStore(client, repo, props);
    }
}
