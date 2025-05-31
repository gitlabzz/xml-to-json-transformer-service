package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import jakarta.servlet.http.HttpServletRequest;

import java.io.InputStream;


@RestController
@RequestMapping("/transform")
public class TransformController {

    private final XmlToJsonStreamer streamer;
    private final AuditService auditService;

    public TransformController(XmlToJsonStreamer streamer, AuditService auditService) {
        this.streamer = streamer;
        this.auditService = auditService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> transform(@RequestBody InputStream xmlStream,
                                                          HttpServletRequest request) {
        long start = System.currentTimeMillis();
        String clientIp = request.getRemoteAddr();

        StreamingResponseBody body = out -> {
            boolean success = false;
            try {
                streamer.transform(xmlStream, out);
                success = true;
            } finally {
                long end = System.currentTimeMillis();
                auditService.add(clientIp, start, end, success, new byte[0], new byte[0]);
            }
        };

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
