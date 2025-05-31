package com.example.transformer;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



@RestController
@RequestMapping("/transform")
public class TransformController {

    private final XmlToJsonStreamer streamer;
    private final AuditService auditService;
    private static final int MAX_CAPTURE_SIZE = 1024 * 1024; // 1MB

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
            ByteArrayOutputStream xmlCopy = new ByteArrayOutputStream();
            ByteArrayOutputStream jsonCopy = new ByteArrayOutputStream();
            InputStream teeIn = new LimitedTeeInputStream(xmlStream, xmlCopy, MAX_CAPTURE_SIZE);
            OutputStream teeOut = new TeeOutputStream(out, new LimitedOutputStream(jsonCopy, MAX_CAPTURE_SIZE));
            boolean success = false;
            try {
                streamer.transform(teeIn, teeOut);
                success = true;
            } finally {
                long end = System.currentTimeMillis();
                auditService.add(clientIp, start, end, success, xmlCopy.toByteArray(), jsonCopy.toByteArray());
            }
        };

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private static class LimitedTeeInputStream extends FilterInputStream {
        private final OutputStream branch;
        private final int limit;
        private int count = 0;

        protected LimitedTeeInputStream(InputStream in, OutputStream branch, int limit) {
            super(in);
            this.branch = branch;
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b != -1 && count < limit) {
                branch.write(b);
                count++;
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n > 0 && count < limit) {
                int toWrite = Math.min(n, limit - count);
                branch.write(b, off, toWrite);
                count += toWrite;
            }
            return n;
        }
    }

    private static class LimitedOutputStream extends OutputStream {
        private final OutputStream delegate;
        private final int limit;
        private int count = 0;

        LimitedOutputStream(OutputStream delegate, int limit) {
            this.delegate = delegate;
            this.limit = limit;
        }

        @Override
        public void write(int b) throws IOException {
            if (count < limit) {
                delegate.write(b);
                count++;
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            int remain = limit - count;
            if (remain <= 0) {
                return;
            }
            int toWrite = Math.min(len, remain);
            delegate.write(b, off, toWrite);
            count += toWrite;
        }
    }

    private static class TeeOutputStream extends OutputStream {
        private final OutputStream out1;
        private final OutputStream out2;

        TeeOutputStream(OutputStream out1, OutputStream out2) {
            this.out1 = out1;
            this.out2 = out2;
        }

        @Override
        public void write(int b) throws IOException {
            out1.write(b);
            out2.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out1.write(b, off, len);
            out2.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            out1.flush();
            out2.flush();
        }
    }
}
