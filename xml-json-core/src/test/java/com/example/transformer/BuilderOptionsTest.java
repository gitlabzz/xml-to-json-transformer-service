package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class BuilderOptionsTest {

    @Test
    public void unwrapRootWithBuilder() throws Exception {
        XmlToJsonStreamer streamer = XmlToJsonStreamer.builder()
                .wrapRootElement(false)
                .build();
        ByteArrayInputStream in = new ByteArrayInputStream("<a>v</a>".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("\"v\"", out.toString());
    }

    @Test
    public void prettyPrintWithBuilder() throws Exception {
        XmlToJsonStreamer streamer = XmlToJsonStreamer.builder()
                .prettyPrint(true)
                .build();
        ByteArrayInputStream in = new ByteArrayInputStream("<x><y>z</y></x>".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertTrue(out.toString().contains("\n"));
    }
}
