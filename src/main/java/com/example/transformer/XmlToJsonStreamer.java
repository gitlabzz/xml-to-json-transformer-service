package com.example.transformer;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

@Component
public class XmlToJsonStreamer {

    public void transform(InputStream xmlInput, OutputStream jsonOutput) throws XMLStreamException, IOException {
        XMLInputFactory inFactory = XMLInputFactory.newFactory();
        XMLStreamReader reader = inFactory.createXMLStreamReader(xmlInput);

        JsonXMLConfig config = new JsonXMLConfigBuilder()
                .autoArray(true)
                .autoPrimitive(false)
                .prettyPrint(false)
                .build();

        JsonXMLOutputFactory outFactory = new JsonXMLOutputFactory(config);
        XMLStreamWriter writer = outFactory.createXMLStreamWriter(jsonOutput);

        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLStreamConstants.START_DOCUMENT:
                    writer.writeStartDocument();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    writer.writeEndDocument();
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    String prefix = reader.getPrefix();
                    String local = reader.getLocalName();
                    String ns = reader.getNamespaceURI();
                    if (ns != null && !ns.isEmpty()) {
                        writer.writeStartElement(prefix, local, ns);
                    } else {
                        writer.writeStartElement(local);
                    }
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        String aprefix = reader.getAttributePrefix(i);
                        String alocal = reader.getAttributeLocalName(i);
                        String ans = reader.getAttributeNamespace(i);
                        String value = reader.getAttributeValue(i);
                        if (ans != null && !ans.isEmpty()) {
                            writer.writeAttribute(aprefix, ans, alocal, value);
                        } else {
                            writer.writeAttribute(alocal, value);
                        }
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    writer.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    writer.writeEndElement();
                    break;
                case XMLStreamConstants.CDATA:
                    writer.writeCData(reader.getText());
                    break;
                case XMLStreamConstants.COMMENT:
                    // omit
                    break;
                default:
                    break;
            }
        }
        writer.flush();
        writer.close();
    }
}
