package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentsAndPITest {

    private final XmlToJsonStreamer streamer = new XmlToJsonStreamer();

    @Test
    public void commentsAndProcessingInstructionsAreIgnored() throws Exception {
        String xml = "<?xml version=\"1.0\"?><a><!-- c --><![CDATA[x]]><?pi hi?></a>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals("{\"a\":\"x\"}", out.toString());
    }
}
