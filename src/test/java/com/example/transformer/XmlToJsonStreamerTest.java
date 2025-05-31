package com.example.transformer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlToJsonStreamerTest {

    private final XmlToJsonStreamer streamer = new XmlToJsonStreamer(new MappingConfig());

    @Test
    public void simpleElement() throws Exception {
        String xml = "<a>Hello</a>";
        String expected = "{\"a\":\"Hello\"}";
        assertEquals(expected, transform(xml));
    }

    @Test
    public void attributes() throws Exception {
        String xml = "<a id=\"1\"/>";
        String expected = "{\"a\":{\"@id\":\"1\"}}";
        assertEquals(expected, transform(xml));
    }

    @Test
    public void mixedContent() throws Exception {
        String xml = "<p>Hello<b>x</b></p>";
        String expected = "{\"p\":{\"#text\":\"Hello\",\"b\":\"x\"}}";
        assertEquals(expected, transform(xml));
    }

    @Test
    public void inputStreamClosed() throws Exception {
        byte[] data = "<a/>".getBytes(StandardCharsets.UTF_8);
        CloseTrackingInputStream in = new CloseTrackingInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        assertTrue(in.closed);
    }

    private static class CloseTrackingInputStream extends ByteArrayInputStream {
        boolean closed = false;

        CloseTrackingInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }

    private String transform(String xml) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(in, out);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
