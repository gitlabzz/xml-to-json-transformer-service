package com.example.transformer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransformerConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "mapping")
    public MappingConfig mappingConfig() {
        return new MappingConfig();
    }

    @Bean
    public XmlToJsonStreamer xmlToJsonStreamer(MappingConfig config) throws java.io.IOException {
        return XmlToJsonStreamer.builder().mappingConfig(config).build();
    }
}
