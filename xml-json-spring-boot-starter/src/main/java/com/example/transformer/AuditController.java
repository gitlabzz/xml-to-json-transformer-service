package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Controller
public class AuditController {
    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);
    private final AuditService service;
    private final AuditProperties props;

    public AuditController(AuditService service, AuditProperties props) {
        this.service = service;
        this.props = props;
    }

    @GetMapping(value = "/audit", produces = MediaType.TEXT_HTML_VALUE)
    public String list(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        logger.info("Audit list requested - page {}", page);
        model.addAttribute("entries", service.page(page, props.getPageSize()));
        model.addAttribute("page", page);
        model.addAttribute("pageSize", props.getPageSize());
        return "auditList";
    }

    @GetMapping(value = "/audit/api", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<AuditEntrySummary> listApi(@RequestParam(name = "page", defaultValue = "0") int page) {
        logger.debug("Audit API list requested - page {}", page);
        return service.page(page, props.getPageSize()).stream()
                .map(AuditEntrySummary::new)
                .toList();
    }

    @GetMapping(value = "/audit/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public Object detail(@PathVariable("id") long id, Model model) throws IOException {
        logger.info("Audit detail requested for id {}", id);
        AuditEntry entry = service.get(id);
        if (entry == null) {
            logger.warn("Audit entry {} not found", id);
            return ResponseEntity.notFound().build();
        }
        model.addAttribute("entry", entry);
        return "auditDetail";
    }
}
