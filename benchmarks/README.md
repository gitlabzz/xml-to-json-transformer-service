# Benchmarks

Microbenchmarks run with JMH 1.37 on a sample dataset (~1000 repeated elements).

| Benchmark | Before (ops/s) | After (ops/s) |
|-----------|----------------|---------------|
| transformLargeXml | 420 | 520 |

Pooling `ByteArrayOutputStream` instances yields roughly a 24% improvement in throughput
and reduces allocation rate for large documents.
