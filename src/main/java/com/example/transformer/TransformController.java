package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import jakarta.servlet.http.HttpServletRequest;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            streamer.transform(xmlStream, buffer);
            long end = System.currentTimeMillis();
            auditService.add(clientIp, start, end, true, new byte[0], new byte[0]);
            byte[] data = buffer.toByteArray();
            StreamingResponseBody body = out -> out.write(data);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        } catch (XMLStreamException | IOException e) {
            long end = System.currentTimeMillis();
            auditService.add(clientIp, start, end, false, new byte[0], new byte[0]);
            return ResponseEntity.badRequest().build();
        }
    }
}
