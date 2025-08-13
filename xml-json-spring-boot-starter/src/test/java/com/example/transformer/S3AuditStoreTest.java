package com.example.transformer;

import com.example.transformer.persistence.AuditEntryRepository;
import com.example.transformer.persistence.JpaAuditEntry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class S3AuditStoreTest {

    @Test
    void generatesPresignedUrl() {
        AuditProperties props = new AuditProperties();
        props.setS3Bucket("b");
        props.setS3Prefix("p");
        props.setS3Region("us-east-1");
        props.setS3Endpoint("http://localhost:9000");

        S3Client s3 = S3Client.builder()
                .region(Region.of(props.getS3Region()))
                .endpointOverride(URI.create(props.getS3Endpoint()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();

        AuditEntryRepository repo = mock(AuditEntryRepository.class);
        JpaAuditEntry entity = new JpaAuditEntry();
        entity.setId(1L);
        entity.setXmlKey("p/1/xml.gz");
        when(repo.findById(1L)).thenReturn(Optional.of(entity));

        S3AuditStore store = new S3AuditStore(s3, repo, props);
        String url = store.xmlUrl(1L);

        assertNotNull(url);
        assertTrue(url.contains("xml.gz"));
    }
}

