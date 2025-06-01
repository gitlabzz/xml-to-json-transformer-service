package com.example.transformer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
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
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XmlToJsonStreamer {

    private static final Logger logger = LoggerFactory.getLogger(XmlToJsonStreamer.class);

    private final JsonFactory jsonFactory;
    private final XMLInputFactory xmlInputFactory;
    private final MappingConfig config;


    public XmlToJsonStreamer(MappingConfig config) throws IOException {
        this(JsonFactory.builder()
                .configure(JsonWriteFeature.ESCAPE_NON_ASCII, config.isEscapeNonAscii())
                .configure(JsonWriteFeature.COMBINE_UNICODE_SURROGATES_IN_UTF8, true)
                .build(),
             XMLInputFactory.newFactory(),
             config);
    }

    public XmlToJsonStreamer() throws IOException {
        this(new MappingConfig());
    }

    public static Builder builder() { return new Builder(); }

    public XmlToJsonStreamer(JsonFactory jsonFactory, XMLInputFactory xmlInputFactory, MappingConfig config) throws IOException {
        this.config = config;
        this.jsonFactory = jsonFactory;
        this.xmlInputFactory = xmlInputFactory;

        this.xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        this.xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

    }

    public static class Builder {
        private JsonFactory jsonFactory;
        private XMLInputFactory xmlInputFactory;
        private MappingConfig mappingConfig = new MappingConfig();

        public Builder jsonFactory(JsonFactory f) { this.jsonFactory = f; return this; }
        public Builder xmlInputFactory(XMLInputFactory f) { this.xmlInputFactory = f; return this; }
        public Builder mappingConfig(MappingConfig c) { this.mappingConfig = c; return this; }
        /**
         * Configure whether the resulting JSON should include the XML root element
         * as a wrapper object.
         */
        public Builder wrapRootElement(boolean b) {
            this.mappingConfig.setWrapRootElement(b);
            return this;
        }

        /**
         * Enable human readable pretty printed JSON output.
         */
        public Builder prettyPrint(boolean b) {
            this.mappingConfig.setPrettyPrint(b);
            return this;
        }

        public XmlToJsonStreamer build() throws IOException {
            if (jsonFactory == null) {
                jsonFactory = JsonFactory.builder()
                        .configure(JsonWriteFeature.ESCAPE_NON_ASCII, mappingConfig.isEscapeNonAscii())
                        .configure(JsonWriteFeature.COMBINE_UNICODE_SURROGATES_IN_UTF8, true)
                        .build();
            }
            if (xmlInputFactory == null) {
                xmlInputFactory = XMLInputFactory.newFactory();
            }
            return new XmlToJsonStreamer(jsonFactory, xmlInputFactory, mappingConfig);
        }
    }

    private String buildQName(String prefix, String local) {
        if (!config.isPreserveNamespaces() || prefix == null || prefix.isEmpty()) {
            return local;
        }
        return prefix + ":" + local;
    }

    public void transform(InputStream xmlInput, OutputStream jsonOutput) throws XMLStreamException, IOException {
        logger.debug("Starting XML to JSON transformation");
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(xmlInput);

        // advance to root element
        while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT) {
            // skip until start element
        }
        String rootName = buildQName(reader.getPrefix(), reader.getLocalName());
        JsonGenerator g = jsonFactory.createGenerator(jsonOutput);

        g.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), config.isEscapeNonAscii());
        g.configure(JsonWriteFeature.COMBINE_UNICODE_SURROGATES_IN_UTF8.mappedFeature(), true);

        if (config.isPrettyPrint()) {
            g.useDefaultPrettyPrinter();
        } else {
            g.setPrettyPrinter(new CompactPrettyPrinter());
        }

        if (config.isWrapRoot()) {
            g.writeStartObject();
            g.writeFieldName(rootName);
            readElement(reader, g);
            g.writeEndObject();
        } else {
            readElement(reader, g);
        }
        g.flush();
        g.close();
        reader.close();
        xmlInput.close();
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
                    state.buffer = new ByteArrayOutputStream(64);
                    JsonGenerator tmp = jsonFactory.createGenerator(state.buffer);
                    tmp.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), config.isEscapeNonAscii());
                    tmp.configure(JsonWriteFeature.COMBINE_UNICODE_SURROGATES_IN_UTF8.mappedFeature(), true);
                    tmp.setPrettyPrinter(new CompactPrettyPrinter());
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
                        tmp.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), config.isEscapeNonAscii());
                        tmp.configure(JsonWriteFeature.COMBINE_UNICODE_SURROGATES_IN_UTF8.mappedFeature(), true);
                        tmp.setPrettyPrinter(new CompactPrettyPrinter());
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

    public void transform(Reader xmlReader, Writer jsonWriter) throws XMLStreamException, IOException {
        logger.debug("Starting XML to JSON transformation");
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(xmlReader);

        while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT) {
        }
        String rootName = buildQName(reader.getPrefix(), reader.getLocalName());
        JsonGenerator g = jsonFactory.createGenerator(jsonWriter);

        g.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), config.isEscapeNonAscii());
        g.configure(JsonWriteFeature.COMBINE_UNICODE_SURROGATES_IN_UTF8.mappedFeature(), true);

        if (config.isPrettyPrint()) {
            g.useDefaultPrettyPrinter();
        } else {
            g.setPrettyPrinter(new CompactPrettyPrinter());
        }

        if (config.isWrapRoot()) {
            g.writeStartObject();
            g.writeFieldName(rootName);
            readElement(reader, g);
            g.writeEndObject();
        } else {
            readElement(reader, g);
        }
        g.flush();
        g.close();
        reader.close();
        xmlReader.close();
        logger.debug("XML to JSON transformation completed");
    }
}
