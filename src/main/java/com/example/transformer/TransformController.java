package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/transform")
public class TransformController {

    private final XmlToJsonStreamer streamer;

    public TransformController(XmlToJsonStreamer streamer) {
        this.streamer = streamer;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transform(@RequestBody byte[] body) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            streamer.transform(new ByteArrayInputStream(body), out);
        } catch (XMLStreamException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"Malformed XML\"}");
        }
        return ResponseEntity.ok(out.toString());
    }
}
