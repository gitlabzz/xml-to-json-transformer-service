package com.example.transformer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class XmlToJsonStreamer {

    private final JsonFactory jsonFactory = new JsonFactory();

    public void transform(InputStream xmlInput, OutputStream jsonOutput) throws XMLStreamException, IOException {
        XMLInputFactory inFactory = XMLInputFactory.newFactory();
        XMLStreamReader reader = inFactory.createXMLStreamReader(xmlInput);

        // advance to root element
        while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT) {
            // skip until start element
        }
        String rootName = reader.getLocalName();
        String rootJson = readElement(reader);

        JsonGenerator g = jsonFactory.createGenerator(jsonOutput);
        g.writeStartObject();
        g.writeFieldName(rootName);
        g.writeRawValue(rootJson);
        g.writeEndObject();
        g.flush();
        g.close();
    }

    private String readElement(XMLStreamReader reader) throws XMLStreamException, IOException {
        Map<String, String> attributes = new LinkedHashMap<>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }
        StringBuilder text = new StringBuilder();
        Map<String, List<String>> children = new LinkedHashMap<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String childName = reader.getLocalName();
                String childJson = readElement(reader);
                children.computeIfAbsent(childName, k -> new ArrayList<>()).add(childJson);
            } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) {
                if (!reader.isWhiteSpace()) {
                    text.append(reader.getText());
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
        }

        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        if (attributes.isEmpty() && children.isEmpty()) {
            g.writeString(text.toString());
        } else {
            g.writeStartObject();
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                g.writeStringField("@" + e.getKey(), e.getValue());
            }
            if (text.length() > 0) {
                g.writeStringField("#text", text.toString());
            }
            for (Map.Entry<String, List<String>> e : children.entrySet()) {
                g.writeFieldName(e.getKey());
                List<String> vals = e.getValue();
                if (vals.size() == 1) {
                    g.writeRawValue(vals.get(0));
                } else {
                    g.writeRawValue(vals.stream().collect(Collectors.joining(",", "[", "]")));
                }
            }
            g.writeEndObject();
        }
        g.close();
        return sw.toString();
    }
}
