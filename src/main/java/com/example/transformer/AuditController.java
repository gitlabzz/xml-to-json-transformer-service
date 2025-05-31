package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class AuditController {
    private final AuditService service;
    private final AuditProperties props;

    public AuditController(AuditService service, AuditProperties props) {
        this.service = service;
        this.props = props;
    }

    @GetMapping(value = "/audit", produces = MediaType.TEXT_HTML_VALUE)
    public String list(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        model.addAttribute("entries", service.page(page, props.getPageSize()));
        model.addAttribute("page", page);
        model.addAttribute("pageSize", props.getPageSize());
        return "auditList";
    }

    @GetMapping(value = "/audit/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String detail(@PathVariable("id") long id, Model model) throws IOException {
        AuditEntry entry = service.get(id);
        if (entry == null) {
            return "auditDetail";
        }
        model.addAttribute("entry", entry);
        return "auditDetail";
    }
}
