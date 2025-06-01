package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NamespaceTest {

    private final XmlToJsonStreamer streamer = XmlToJsonStreamer.builder()
            .mappingConfig(new MappingConfig())
            .build();

    public NamespaceTest() throws IOException {
    }

    @Test
    public void prefixesPreserved() throws Exception {
        String xml = "<ns:root xmlns:ns='urn:test'><ns:child attr='v'/></ns:root>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("{\"ns:root\":{\"ns:child\":{\"@attr\":\"v\"}}}", out.toString());
    }
}
