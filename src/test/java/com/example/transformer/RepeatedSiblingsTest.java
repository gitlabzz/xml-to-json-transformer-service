package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepeatedSiblingsTest {

    private final XmlToJsonStreamer streamer = new XmlToJsonStreamer(new MappingConfig());

    @Test
    public void arrayLogic() throws Exception {
        String xml = "<items><item>x</item><item>y</item></items>";
        String expected = "{\"items\":{\"item\":[\"x\",\"y\"]}}";
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertEquals(expected, out.toString());
    }
}
