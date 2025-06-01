package com.example.transformer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class LargeStreamTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void memoryProfile() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<items>");
        for (int i = 0; i < 100000; i++) {
            sb.append("<item>").append(i).append("</item>");
        }
        sb.append("</items>");
        byte[] xml = sb.toString().getBytes();

        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        MemoryUsage before = bean.getHeapMemoryUsage();
        mockMvc.perform(post("/transform")
                .contentType(MediaType.APPLICATION_XML)
                .content(xml))
                .andReturn();
        MemoryUsage after = bean.getHeapMemoryUsage();
        long diff = after.getUsed() - before.getUsed();
        assertTrue(diff < 50 * 1024 * 1024);
    }

    @Test
    public void streamerLargeDocument() throws Exception {
        XmlToJsonStreamer streamer = XmlToJsonStreamer.builder()
                .mappingConfig(new MappingConfig())
                .build();

        StringBuilder sb = new StringBuilder();
        sb.append("<items>");
        for (int i = 0; i < 100000; i++) {
            sb.append("<item>").append(i).append("</item>");
        }
        sb.append("</items>");
        byte[] xml = sb.toString().getBytes(StandardCharsets.UTF_8);

        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        MemoryUsage before = bean.getHeapMemoryUsage();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(new ByteArrayInputStream(xml), out);
        MemoryUsage after = bean.getHeapMemoryUsage();
        long diff = after.getUsed() - before.getUsed();
        assertTrue(diff < 50 * 1024 * 1024);

        String json = out.toString(StandardCharsets.UTF_8);
        JsonFactory factory = new JsonFactory();
        JsonParser p = factory.createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("items", p.nextFieldName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("item", p.nextFieldName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        int count = 0;
        while (p.nextToken() != JsonToken.END_ARRAY) {
            p.getValueAsString();
            count++;
        }
        assertEquals(100000, count);
    }
}
