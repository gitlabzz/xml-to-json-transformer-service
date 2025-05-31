package com.example.transformer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.example.transformer.CompactPrettyPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class XmlToJsonStreamer {

    private static final Logger logger = LoggerFactory.getLogger(XmlToJsonStreamer.class);

    /**
     * {@link CharacterEscapes} implementation that suppresses unnecessary
     * escaping so all Unicode code points are written as raw UTF-8.
     */
    public static final class NoAsciiEscapes extends CharacterEscapes {
        private static final long serialVersionUID = 1L;

        private static final int[] ESC = CharacterEscapes.standardAsciiEscapesForJSON();

        static {
            for (int i = 32; i < ESC.length; i++) {
                if (i != '"' && i != '\\') {
                    ESC[i] = CharacterEscapes.ESCAPE_NONE;
                }
            }
        }

        @Override
        public int[] getEscapeCodesForAscii() {
            return ESC;
        }

        /**
         * Never escape any non-ASCII code point
         */
        @Override
        public SerializedString getEscapeSequence(int ch) {
            return null;
        }
    }

    private final JsonFactory jsonFactory;
    private final MappingConfig config;

    @Autowired
    public XmlToJsonStreamer(MappingConfig config) {
        this.config = config;
        JsonFactory f = JsonFactory.builder()
                .disable(JsonWriteFeature.ESCAPE_NON_ASCII)
                .build();
        this.jsonFactory = f;
    }

    public XmlToJsonStreamer() {
        this(new MappingConfig());
    }

    private String buildQName(String prefix, String local) {
        if (prefix == null || prefix.isEmpty()) {
            return local;
        }
        return prefix + ":" + local;
    }

    public void transform(InputStream xmlInput, OutputStream jsonOutput) throws XMLStreamException, IOException {
        logger.debug("Starting XML to JSON transformation");
        XMLInputFactory inFactory = XMLInputFactory.newFactory();
        inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        XMLStreamReader reader = inFactory.createXMLStreamReader(xmlInput);

        // advance to root element
        while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT) {
            // skip until start element
        }
        String rootName = buildQName(reader.getPrefix(), reader.getLocalName());
        JsonGenerator g = jsonFactory.createGenerator(jsonOutput);
        g.setHighestNonEscapedChar(0);
        g.setCharacterEscapes(new NoAsciiEscapes());
        g.setPrettyPrinter(new CompactPrettyPrinter());
        g.writeStartObject();
        g.writeFieldName(rootName);
        readElement(reader, g);
        g.writeEndObject();
        g.flush();
        g.close();
        reader.close();
        xmlInput.close();
        logger.debug("XML to JSON transformation completed");
    }

    private static class ChildState {
        final List<String> fragments = new ArrayList<>();
        String lastFragment;
        int count = 0;

        void add(String fragment) {
            fragments.add(fragment);
            lastFragment = fragment;
            count++;
        }
    }

    private void readElement(XMLStreamReader reader, JsonGenerator out) throws XMLStreamException, IOException {
        Map<String, String> attributes = new LinkedHashMap<>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = buildQName(reader.getAttributePrefix(i), reader.getAttributeLocalName(i));
            attributes.put(name, reader.getAttributeValue(i));
        }

        StringBuilder text = new StringBuilder();
        Map<String, ChildState> children = new LinkedHashMap<>();

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        JsonGenerator tmp = jsonFactory.createGenerator(buf);
        tmp.setHighestNonEscapedChar(0);
        tmp.setCharacterEscapes(new NoAsciiEscapes());
        tmp.setPrettyPrinter(new CompactPrettyPrinter());
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String childName = buildQName(reader.getPrefix(), reader.getLocalName());
                ChildState state = children.computeIfAbsent(childName, n -> new ChildState());
                buf.reset();
                readElement(reader, tmp);
                tmp.flush();
                state.add(buf.toString(StandardCharsets.UTF_8));
            } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                if (!reader.isWhiteSpace()) {
                    text.append(reader.getText());
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }
        tmp.close();

        if (attributes.isEmpty() && children.isEmpty()) {
            out.writeString(text.toString());
        } else {
            out.writeStartObject();
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                out.writeStringField(config.getAttributePrefix() + e.getKey(), e.getValue());
            }
            if (text.length() > 0) {
                out.writeStringField(config.getTextField(), text.toString());
            }
            for (Map.Entry<String, ChildState> e : children.entrySet()) {
                String name = e.getKey();
                ChildState state = e.getValue();
                out.writeFieldName(name);
                if (!config.isArraysForRepeatedSiblings()) {
                    out.writeRawValue(state.lastFragment);
                } else if (state.count == 1) {
                    out.writeRawValue(state.fragments.get(0));
                } else {
                    out.writeStartArray();
                    for (String fragment : state.fragments) {
                        out.writeRawValue(fragment);
                    }
                    out.writeEndArray();
                }
            }
            out.writeEndObject();
        }
    }
}
