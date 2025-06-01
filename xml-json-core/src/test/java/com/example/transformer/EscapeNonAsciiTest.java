package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EscapeNonAsciiTest {

    @Test
    public void escapeAllNonAscii() throws Exception {
        MappingConfig cfg = new MappingConfig();
        cfg.setEscapeNonAscii(true);
        XmlToJsonStreamer streamer = XmlToJsonStreamer.builder()
                .mappingConfig(cfg)
                .build();
        String xml = "<msg>π 🐨</msg>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("{\"msg\":\"\\u03C0 \\uD83D\\uDC28\"}", out.toString(StandardCharsets.UTF_8));
    }
}
