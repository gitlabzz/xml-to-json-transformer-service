package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnicodeTest {

    private final XmlToJsonStreamer streamer = new XmlToJsonStreamer();

    @Test
    public void utf8Correctness() throws Exception {
        String xml = "<msg>🐨</msg>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("{\"msg\":\"🐨\"}", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }
}
