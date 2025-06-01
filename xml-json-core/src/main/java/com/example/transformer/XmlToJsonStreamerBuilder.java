package com.example.transformer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonWriteFeature;

import javax.xml.stream.XMLInputFactory;
import java.io.IOException;
import java.util.function.Consumer;

public final class XmlToJsonStreamerBuilder {
    private MappingConfig config = new MappingConfig();
    private JsonFactory jsonFactory;
    private XMLInputFactory xmlFactory;

    public XmlToJsonStreamerBuilder config(Consumer<MappingConfig> c) {
        c.accept(config);
        return this;
    }
    public XmlToJsonStreamerBuilder jsonFactory(JsonFactory jf) {
        this.jsonFactory = jf;
        return this;
    }
    public XmlToJsonStreamerBuilder xmlFactory(XMLInputFactory xf) {
        this.xmlFactory = xf;
        return this;
    }

    public XmlToJsonStreamer build() throws IOException {
        if (jsonFactory == null) {
            jsonFactory = JsonFactory.builder()
                    .configure(JsonWriteFeature.ESCAPE_NON_ASCII, false)
                    .build();
        }
        if (xmlFactory == null) {
            xmlFactory = XMLInputFactory.newFactory();
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        }
        return new XmlToJsonStreamer(jsonFactory, xmlFactory, config);
    }
}
