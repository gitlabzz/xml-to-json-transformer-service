package com.example.transformer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper; // --- ADDED ---
import com.fasterxml.jackson.databind.SerializationFeature; // --- ADDED ---

import javax.xml.transform.OutputKeys; // --- ADDED ---
import javax.xml.transform.Transformer; // --- ADDED ---
import javax.xml.transform.TransformerFactory; // --- ADDED ---
import javax.xml.transform.stream.StreamResult; // --- ADDED ---
import javax.xml.transform.stream.StreamSource; // --- ADDED ---
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class AuditEntry implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final long id;
    private final String clientIp;
    private final long requestTime;
    private final long responseTime;
    private final boolean success;
    private final long durationMs;
    @JsonAlias("xml")
    private final byte[] xmlData;
    @JsonAlias("json")
    private final byte[] jsonData;
    private final boolean compressed;

    // --- No changes to the constructor ---
    @JsonCreator
    public AuditEntry(
            @JsonProperty("id") long id,
            @JsonProperty("clientIp") String clientIp,
            @JsonProperty("requestTime") long requestTime,
            @JsonProperty("responseTime") long responseTime,
            @JsonProperty("success") boolean success,
            @JsonProperty("durationMs") long durationMs,
            @JsonProperty("xmlData")
            @JsonAlias("xml") byte[] xmlData,
            @JsonProperty("jsonData")
            @JsonAlias("json") byte[] jsonData,
            @JsonProperty("compressed") boolean compressed) {
        this.id = id;
        this.clientIp = clientIp;
        this.requestTime = requestTime;
        this.responseTime = responseTime;
        this.success = success;
        this.durationMs = durationMs;
        this.xmlData = xmlData;
        this.jsonData = jsonData;
        this.compressed = compressed;
    }

    // --- No changes to simple getters ---
    public long getId() { return id; }
    public String getClientIp() { return clientIp; }
    public long getRequestTime() { return requestTime; }
    public long getResponseTime() { return responseTime; }
    public boolean isSuccess() { return success; }
    public long getDurationMs() { return durationMs; }


    // --- MODIFIED getXml() method ---
    public String getXml() throws IOException {
        byte[] decompressed = decompress(xmlData);
        if (decompressed.length == 0) return "";
        try {
            return prettyPrintXml(new String(decompressed, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            // Fallback to raw string if formatting fails
            return new String(decompressed, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // --- MODIFIED getJson() method ---
    public String getJson() throws IOException {
        byte[] decompressed = decompress(jsonData);
        if (decompressed.length == 0) return "";
        try {
            return prettyPrintJson(new String(decompressed, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            // Fallback to raw string if formatting fails
            return new String(decompressed, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // --- ADDED: Helper method for pretty-printing JSON ---
    private String prettyPrintJson(String rawJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Read the raw JSON into a generic Object
        Object jsonObject = mapper.readValue(rawJson, Object.class);
        // Re-write it with indentation enabled
        return mapper.enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(jsonObject);
    }

    // --- ADDED: Helper method for pretty-printing XML ---
    private String prettyPrintXml(String rawXml) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // This is a security measure to prevent certain types of attacks
        transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
        transformerFactory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalStylesheet", "");

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); // Indent by 2 spaces
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // Optional: remove <?xml ...>

        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        StreamSource source = new StreamSource(new StringReader(rawXml));
        transformer.transform(source, result);
        return writer.toString();
    }


    private byte[] decompress(byte[] data) throws IOException {
        // --- No changes to decompress() or compress() ---
        if (!compressed) {
            return data;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             GZIPInputStream gis = new GZIPInputStream(bis)) {
            return gis.readAllBytes();
        }
    }

    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }
}