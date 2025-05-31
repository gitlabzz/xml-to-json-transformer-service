package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import jakarta.servlet.http.HttpServletRequest;

import javax.xml.stream.XMLStreamException;
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
    public ResponseEntity<StreamingResponseBody> transform(HttpServletRequest request) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            streamer.transform(request.getInputStream(), buffer);
        } catch (XMLStreamException e) {
            StreamingResponseBody errBody = out -> out.write("{\"error\":\"Malformed XML\"}".getBytes());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(errBody);
        } catch (IOException e) {
            StreamingResponseBody errBody = out -> out.write(("{\"error\":\"" + e.getMessage() + "\"}").getBytes());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(errBody);
        }

        byte[] json = buffer.toByteArray();
        StreamingResponseBody body = out -> out.write(json);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
