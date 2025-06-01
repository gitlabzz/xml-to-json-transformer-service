package com.example.transformer;

import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InvalidXmlTest {
    @Test
    void invalidXmlThrows() {
        XmlToJsonStreamer s = new XmlToJsonStreamerBuilder().build();
        assertThrows(XMLStreamException.class, () -> {
            s.transform(new ByteArrayInputStream("<x>".getBytes()), new ByteArrayOutputStream());
        });
    }
}
