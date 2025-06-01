package com.example.transformer;

import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class XxeTest {

    private final XmlToJsonStreamer streamer = XmlToJsonStreamer.builder()
            .mappingConfig(new MappingConfig())
            .build();

    public XxeTest() throws IOException {
    }

    @Test
    public void externalEntityFails() {
        String xml = "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><foo>&xxe;</foo>";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(XMLStreamException.class, () -> streamer.transform(in, out));
    }
}
