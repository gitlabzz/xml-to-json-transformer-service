package com.example.transformer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.transformer.ByteArrayOutputStreamPool;

@Configuration
public class TransformerConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "mapping")
    public MappingConfig mappingConfig() {
        return new MappingConfig();
    }

    @Bean
    public ByteArrayOutputStreamPool byteArrayOutputStreamPool() {
        return new ByteArrayOutputStreamPool();
    }

    @Bean
    @RefreshScope
    public XmlToJsonStreamer xmlToJsonStreamer(MappingConfig config, ByteArrayOutputStreamPool pool) throws java.io.IOException {
        return XmlToJsonStreamer.builder().mappingConfig(config).bufferPool(pool).build();
    }
}
