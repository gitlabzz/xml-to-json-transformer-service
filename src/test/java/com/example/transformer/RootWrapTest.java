package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RootWrapTest {

    @Test
    public void unwrapRoot() throws Exception {
        MappingConfig cfg = new MappingConfig();
        cfg.setWrapRoot(false);
        XmlToJsonStreamer streamer = new XmlToJsonStreamer(cfg);
        String xml = "<a>v</a>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("\"v\"", out.toString());
    }

    @Test
    public void readerWriter() throws Exception {
        MappingConfig cfg = new MappingConfig();
        cfg.setPrettyPrint(true);
        XmlToJsonStreamer streamer = new XmlToJsonStreamer(cfg);
        java.io.StringReader reader = new java.io.StringReader("<x><y>z</y></x>");
        java.io.StringWriter writer = new java.io.StringWriter();
        streamer.transform(reader, writer);
        String expected = "{\n  \"x\" : {\n    \"y\" : \"z\"\n  }\n}";
        assertEquals(expected, writer.toString().trim());
    }
}
