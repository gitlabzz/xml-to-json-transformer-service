package com.example.transformer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javax.xml.stream.XMLStreamException;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

@Component
public class XmlToJsonStreamer {

    public void transform(InputStream xmlInput, OutputStream jsonOutput) throws XMLStreamException, IOException {
        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            JsonNode node = xmlMapper.readTree(xmlInput);
            jsonMapper.writeValue(jsonOutput, node);
        } catch (JsonProcessingException e) {
            throw new XMLStreamException("Invalid XML", e);
        }
    }
}
