package com.example.transformer;

import com.example.transformer.persistence.AuditEntryRepository;
import com.example.transformer.persistence.JpaAuditEntry;
import org.springframework.data.domain.PageRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Stores audit payloads in S3 compatible object storage while keeping metadata in the database.
 */
public class S3AuditStore implements AuditStore {
    private final S3Client s3;
    private final AuditEntryRepository repo;
    private final String bucket;
    private final String prefix;
    private final S3Presigner presigner;

    public S3AuditStore(S3Client s3, AuditEntryRepository repo, AuditProperties props) {
        this.s3 = s3;
        this.repo = repo;
        this.bucket = props.getS3Bucket();
        this.prefix = props.getS3Prefix();
        S3Presigner.Builder b = S3Presigner.builder();
        if (props.getS3Endpoint() != null) {
            b.endpointOverride(URI.create(props.getS3Endpoint()));
        }
        if (props.getS3Region() != null) {
            b.region(Region.of(props.getS3Region()));
        }
        this.presigner = b.build();
    }

    @Override
    public void save(AuditEntry e) {
        JpaAuditEntry j = new JpaAuditEntry();
        j.setClientIp(e.getClientIp());
        j.setRequestTime(e.getRequestTime());
        j.setResponseTime(e.getResponseTime());
        j.setSuccess(e.isSuccess());
        j.setDurationMs(e.getDurationMs());
        j.setCompressed(true);
        try {
            j.setXmlTextExcerpt(takeExcerpt(e.getXml(), 2048));
            j.setJsonTextExcerpt(takeExcerpt(e.getJson(), 2048));
        } catch (IOException ex) {
            // ignore excerpt on error
        }
        repo.save(j);
        long id = j.getId();
        String base = prefix + "/" + id;
        try {
            put(base + "/xml.gz", e.getXml().getBytes(StandardCharsets.UTF_8));
            put(base + "/json.gz", e.getJson().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        j.setXmlKey(base + "/xml.gz");
        j.setJsonKey(base + "/json.gz");
        j.setXmlData(null);
        j.setJsonData(null);
        repo.save(j);
    }

    private void put(String key, byte[] raw) throws IOException {
        byte[] gz = AuditEntry.compress(raw);
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromBytes(gz));
    }

    @Override
    public List<AuditEntry> page(int page, int size) {
        return repo.findAll(PageRequest.of(page, size))
                .map(j -> new AuditEntry(j.getId(), j.getClientIp(), j.getRequestTime(), j.getResponseTime(),
                        j.isSuccess(), j.getDurationMs(), new byte[0], new byte[0], false))
                .getContent();
    }

    @Override
    public AuditEntry get(long id) {
        return repo.findById(id)
                .map(j -> new AuditEntry(j.getId(), j.getClientIp(), j.getRequestTime(), j.getResponseTime(),
                        j.isSuccess(), j.getDurationMs(), new byte[0], new byte[0], false))
                .orElse(null);
    }

    @Override
    public PageResult<AuditEntry> search(String q, int page, int size) {
        var p = repo.search(q, PageRequest.of(page, size))
                .map(j -> new AuditEntry(j.getId(), j.getClientIp(), j.getRequestTime(), j.getResponseTime(),
                        j.isSuccess(), j.getDurationMs(), new byte[0], new byte[0], false));
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

    @Override
    public String xmlUrl(long id) {
        return urlFor(id, true);
    }

    @Override
    public String jsonUrl(long id) {
        return urlFor(id, false);
    }

    private String urlFor(long id, boolean xml) {
        return repo.findById(id).map(j -> {
            String key = xml ? j.getXmlKey() : j.getJsonKey();
            if (key == null) {
                return null;
            }
            GetObjectRequest req = GetObjectRequest.builder().bucket(bucket).key(key).build();
            PresignedGetObjectRequest presigned = presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .getObjectRequest(req)
                            .signatureDuration(Duration.ofMinutes(5))
                            .build());
            return presigned.url().toString();
        }).orElse(null);
    }
}

