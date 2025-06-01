package com.example.transformer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransformerConfiguration {
    @Bean
    public XmlToJsonStreamer xmlToJsonStreamer(MappingConfig config) throws java.io.IOException {
        return new XmlToJsonStreamer(config);
    }
}
