package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


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
    public void transform(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long start = System.currentTimeMillis();
        String clientIp = request.getRemoteAddr();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ByteArrayOutputStream xmlBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream jsonBuf = new ByteArrayOutputStream();

        InputStream in = new TeeInputStream(request.getInputStream(), xmlBuf);
        OutputStream out = new TeeOutputStream(response.getOutputStream(), jsonBuf);
        boolean success = false;
        try {
            streamer.transform(in, out);
            success = true;
        } catch (XMLStreamException e) {
            response.reset();
            response.setStatus(400);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            byte[] msg = e.getMessage() == null ? new byte[0] : e.getMessage().getBytes(StandardCharsets.UTF_8);
            response.getOutputStream().write(msg);
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            try { out.close(); } catch (IOException ignore) {}
            long end = System.currentTimeMillis();
            auditService.add(clientIp, start, end, success, xmlBuf.toByteArray(), jsonBuf.toByteArray());
        }
    }

    private static class TeeInputStream extends java.io.FilterInputStream {
        private final ByteArrayOutputStream copy;
        protected TeeInputStream(InputStream in, ByteArrayOutputStream copy) {
            super(in);
            this.copy = copy;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b != -1) {
                copy.write(b);
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n > 0) {
                copy.write(b, off, n);
            }
            return n;
        }
    }

    private static class TeeOutputStream extends java.io.FilterOutputStream {
        private final ByteArrayOutputStream copy;
        TeeOutputStream(OutputStream out, ByteArrayOutputStream copy) {
            super(out);
            this.copy = copy;
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            copy.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
            copy.write(b, off, len);
        }
    }
}
