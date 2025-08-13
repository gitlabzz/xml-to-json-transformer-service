package com.example.transformer.api.v1;

import com.example.transformer.AuditEntry;
import com.example.transformer.AuditEntrySummary;
import com.example.transformer.AuditService;
import com.example.transformer.PageResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/audit")
public class AuditControllerV1 {
    private final AuditService service;

    public AuditControllerV1(AuditService service) {
        this.service = service;
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageResult<AuditEntrySummary> search(@RequestParam("q") String q,
                                                @RequestParam(name = "page", defaultValue = "0") int page,
                                                @RequestParam(name = "size", defaultValue = "50") int size) {
        PageResult<AuditEntry> result = service.search(q, page, size);
        return new PageResult<>(
                result.items().stream().map(AuditEntrySummary::new).toList(),
                result.page(), result.size(), result.total());
    }
}
