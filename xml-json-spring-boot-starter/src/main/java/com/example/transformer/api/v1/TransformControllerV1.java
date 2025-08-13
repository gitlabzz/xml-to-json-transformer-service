package com.example.transformer.api.v1;

import com.example.transformer.AuditProperties;
import com.example.transformer.AuditService;
import com.example.transformer.XmlToJsonStreamer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;

@RestController
@RequestMapping("/v1/transform")
public class TransformControllerV1 {
  private final XmlToJsonStreamer streamer;
  private final AuditService auditService;
  private final AuditProperties auditProperties;

  public TransformControllerV1(XmlToJsonStreamer streamer, AuditService auditService, AuditProperties auditProperties) {
    this.streamer = streamer; this.auditService = auditService; this.auditProperties = auditProperties;
  }

  @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
               produces = MediaType.APPLICATION_JSON_VALUE)
  public void transform(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long start = System.currentTimeMillis();
    String clientIp = request.getRemoteAddr();
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ByteArrayOutputStream xmlBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream jsonBuf = new ByteArrayOutputStream();
    InputStream in = new FilterInputStream(request.getInputStream()) {
      public int read() throws IOException { int b = super.read(); if (b!=-1) xmlBuf.write(b); return b; }
      public int read(byte[] b,int off,int len) throws IOException { int n=super.read(b,off,len); if(n>0) xmlBuf.write(b,off,n); return n; }
    };
    OutputStream out = new FilterOutputStream(response.getOutputStream()) {
      public void write(int b) throws IOException { super.write(b); jsonBuf.write(b); }
      public void write(byte[] b,int off,int len) throws IOException { super.write(b,off,len); jsonBuf.write(b,off,len); }
    };

    boolean success=false;
    try { streamer.transform(in, out); success=true; }
    catch (XMLStreamException e) {
      response.reset(); response.setStatus(400); response.setContentType(MediaType.TEXT_PLAIN_VALUE);
      response.getOutputStream().write((e.getMessage()==null?"":e.getMessage()).getBytes(StandardCharsets.UTF_8));
    } finally {
      try { in.close(); } catch (IOException ignore) {}
      try { out.close(); } catch (IOException ignore) {}
      long end = System.currentTimeMillis();
      if (auditProperties.isEnabled()) {
        auditService.add(clientIp, start, end, success, xmlBuf.toByteArray(), jsonBuf.toByteArray());
      }
    }
  }
}
