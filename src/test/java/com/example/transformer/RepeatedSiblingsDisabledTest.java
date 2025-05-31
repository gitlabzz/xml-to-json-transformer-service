package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepeatedSiblingsDisabledTest {

    @Test
    public void noArrayUsesLastValue() throws Exception {
        MappingConfig cfg = new MappingConfig();
        cfg.setArraysForRepeatedSiblings(false);
        XmlToJsonStreamer streamer = new XmlToJsonStreamer(cfg);
        String xml = "<items><item>x</item><item>y</item></items>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("{\"items\":{\"item\":\"y\"}}", out.toString());
    }
}
