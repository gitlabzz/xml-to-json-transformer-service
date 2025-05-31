package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import jakarta.servlet.http.HttpServletRequest;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;


@RestController
@RequestMapping("/transform")
public class TransformController {

    private static final Logger logger = LoggerFactory.getLogger(TransformController.class);

    private final XmlToJsonStreamer streamer;
    private final AuditService auditService;

    public TransformController(XmlToJsonStreamer streamer, AuditService auditService) {
        this.streamer = streamer;
        this.auditService = auditService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> transform(@RequestBody byte[] xmlBytes,
                                                          HttpServletRequest request) throws IOException {
        long start = System.currentTimeMillis();
        String clientIp = request.getRemoteAddr();
        logger.info("Transform request from {} started", clientIp);

        ByteArrayInputStream xmlInput = new ByteArrayInputStream(xmlBytes);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        boolean success = false;
        try {
            streamer.transform(xmlInput, buffer);
            success = true;
        } catch (XMLStreamException e) {
            logger.error("Malformed XML received from {}", clientIp, e);
            StreamingResponseBody errBody = out -> out.write("{\"error\":\"Malformed XML\"}".getBytes());
            auditService.add(clientIp, start, System.currentTimeMillis(), false, xmlBytes, errBody.toString().getBytes());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(errBody);
        } catch (IOException e) {
            logger.error("I/O error during transform for {}", clientIp, e);
            StreamingResponseBody errBody = out -> out.write(("{\"error\":\"" + e.getMessage() + "\"}").getBytes());
            auditService.add(clientIp, start, System.currentTimeMillis(), false, xmlBytes, errBody.toString().getBytes());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(errBody);
        }

        byte[] json = buffer.toByteArray();
        long end = System.currentTimeMillis();
        auditService.add(clientIp, start, end, success, xmlBytes, json);
        logger.info("Transform request from {} completed in {} ms", clientIp, end - start);
        StreamingResponseBody body = out -> out.write(json);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
