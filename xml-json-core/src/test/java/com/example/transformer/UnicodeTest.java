package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnicodeTest {

    private final XmlToJsonStreamer streamer = XmlToJsonStreamer.builder()
            .mappingConfig(new MappingConfig())
            .build();

    public UnicodeTest() throws IOException {
    }

    @Test
    public void utf8Correctness() throws Exception {
        String xml = "<msg>🐨</msg>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("{\"msg\":\"🐨\"}", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void utf8ReaderWriter() throws Exception {
        String xml = "<msg>🐨</msg>";
        StringReader reader = new StringReader(xml);
        StringWriter writer = new StringWriter();
        streamer.transform(reader, writer);
        assertEquals("{\"msg\":\"🐨\"}", writer.toString());
    }

    @Test
    public void diverseCharacters() throws Exception {
        String xml = "<msg>áπ🐨漢字</msg>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("{\"msg\":\"áπ🐨漢字\"}", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }
}
