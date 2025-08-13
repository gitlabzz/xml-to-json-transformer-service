package com.example.transformer.benchmarks;

import com.example.transformer.MappingConfig;
import com.example.transformer.XmlToJsonStreamer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@State(Scope.Benchmark)
public class TransformBenchmarks {

    private XmlToJsonStreamer streamer;
    private String largeXml;

    @Setup
    public void setup() throws Exception {
        MappingConfig config = new MappingConfig();
        streamer = XmlToJsonStreamer.builder().mappingConfig(config).build();
        StringBuilder sb = new StringBuilder();
        sb.append("<root>");
        for (int i = 0; i < 1000; i++) {
            sb.append("<item>" + i + "</item>");
        }
        sb.append("</root>");
        largeXml = sb.toString();
    }

    @Benchmark
    public void transformLargeXml(Blackhole bh) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        streamer.transform(new ByteArrayInputStream(largeXml.getBytes()), out);
        bh.consume(out.size());
    }
}
