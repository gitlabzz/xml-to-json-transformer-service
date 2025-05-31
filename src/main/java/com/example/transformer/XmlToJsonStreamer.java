package com.example.transformer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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

    private final JsonFactory jsonFactory = new JsonFactory();
    private final MappingConfig config;

    @Autowired
    public XmlToJsonStreamer(MappingConfig config) {
        this.config = config;
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
        g.writeStartObject();
        g.writeFieldName(rootName);
        readElement(reader, g);
        g.writeEndObject();
        g.flush();
        g.close();
        logger.debug("XML to JSON transformation completed");
    }

    private static class ChildState {
        ByteArrayOutputStream buffer;
        int count;
        boolean arrayStarted;
    }

    private void readElement(XMLStreamReader reader, JsonGenerator out) throws XMLStreamException, IOException {
        Map<String, String> attributes = new LinkedHashMap<>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = buildQName(reader.getAttributePrefix(i), reader.getAttributeLocalName(i));
            attributes.put(name, reader.getAttributeValue(i));
        }

        StringBuilder text = new StringBuilder();
        Map<String, ChildState> children = new LinkedHashMap<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String childName = buildQName(reader.getPrefix(), reader.getLocalName());
                ChildState state = children.get(childName);
                if (state == null) {
                    state = new ChildState();
                    state.count = 1;
                    state.buffer = new ByteArrayOutputStream();
                    JsonGenerator tmp = jsonFactory.createGenerator(state.buffer);
                    readElement(reader, tmp);
                    tmp.close();
                    children.put(childName, state);
                } else {
                    state.count++;
                    if (config.isArraysForRepeatedSiblings()) {
                        if (!state.arrayStarted) {
                            out.writeFieldName(childName);
                            out.writeStartArray();
                            out.writeRawValue(state.buffer.toString(StandardCharsets.UTF_8));
                            state.arrayStarted = true;
                            state.buffer = null;
                        }
                        readElement(reader, out);
                    } else {
                        state.buffer.reset();
                        JsonGenerator tmp = jsonFactory.createGenerator(state.buffer);
                        readElement(reader, tmp);
                        tmp.close();
                    }
                }
            } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                if (!reader.isWhiteSpace()) {
                    text.append(reader.getText());
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }

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
                ChildState state = e.getValue();
                String name = e.getKey();
                if (state.count == 1 || !config.isArraysForRepeatedSiblings()) {
                    out.writeFieldName(name);
                    out.writeRawValue(state.buffer.toString(StandardCharsets.UTF_8));
                } else if (state.arrayStarted) {
                    out.writeEndArray();
                }
            }
            out.writeEndObject();
        }
    }
}
