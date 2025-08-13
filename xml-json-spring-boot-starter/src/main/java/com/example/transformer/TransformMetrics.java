package com.example.transformer;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class TransformMetrics {
  private final Timer time;
  private final DistributionSummary bytesIn;
  private final DistributionSummary bytesOut;

  public TransformMetrics(MeterRegistry r) {
    this.time = Timer.builder("transform.duration").publishPercentileHistogram(true).register(r);
    this.bytesIn = DistributionSummary.builder("transform.bytes_in").publishPercentileHistogram(true).register(r);
    this.bytesOut = DistributionSummary.builder("transform.bytes_out").publishPercentileHistogram(true).register(r);
  }

  public Timer.Sample start() { return Timer.start(); }
  public void record(Timer.Sample s) { s.stop(time); }
  public void recordIn(long n) { bytesIn.record(n); }
  public void recordOut(long n) { bytesOut.record(n); }
}
