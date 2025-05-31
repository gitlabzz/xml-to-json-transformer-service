package com.example.transformer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MappingConfig.class)
public class XmlJsonTransformerApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmlJsonTransformerApplication.class, args);
    }
}
