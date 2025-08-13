# XML to JSON Transformer

This project provides a Spring Boot service that converts arbitrary XML documents to JSON without loading the entire document in memory. It streams the XML using StAX and writes JSON directly via Jackson.

## Usage

Run tests:

The repository includes a GitHub Actions workflow to automatically run these tests on every push and pull request.

```bash
mvn clean test
```

Start the service (optional):

```bash
mvn spring-boot:run
```

POST XML to `/v1/transform` and receive the mapped JSON. The controller streams the request and response bodies so large documents do not overwhelm memory. The legacy `/transform` endpoint remains available but is deprecated.

## Mapping Rules

* Elements become object keys using their qualified name.
* Attributes are prefixed with `@` (configurable).
* Mixed content uses `#text` for the text value alongside children (configurable).
* Repeated sibling elements are emitted as arrays (can be disabled).
* Comments and processing instructions are ignored.
* Namespace prefixes are preserved.
* Values remain strings.

The tests also cover unicode handling, repeated siblings and ignoring XML comments.

### Configuration

`MappingConfig` exposes the following properties which can be overridden via `application.yml` or configured programmatically using the fluent `new XmlToJsonStreamerBuilder()` API:

```
mapping.attribute-prefix=@
mapping.text-field=#text
mapping.arrays-for-repeated-siblings=true
mapping.wrap-root=true
mapping.pretty-print=false
mapping.preserve-namespaces=true
mapping.escape-non-ascii=false
```

These allow customizing how attributes, text content and repeated elements are represented in the produced JSON.
Root wrapping can be disabled by setting `mapping.wrap-root=false` or using
`new XmlToJsonStreamerBuilder().config(c -> c.setWrapRoot(false))` so that the
children of the XML root element become the top level JSON fields. Human
readable formatting can be enabled via `mapping.pretty-print=true` or
`new XmlToJsonStreamerBuilder().config(c -> c.setPrettyPrint(true))`.
Additional options control namespace handling and escaping of non ASCII characters.

### Audit History

The service keeps a bounded history of recent transformations. The history can be stored either in memory (default) or persisted to a file or external backends. The history size, page size for the HTML views, backend type and whether the stored payloads are compressed can be configured using the following properties:

```
audit.history-size=100
audit.page-size=20
audit.compress=true
audit.backend=memory # or 'file', 'jdbc', 's3'
audit.file-path=audit-store.ser
audit.s3-prefix=audits
# when using object storage
audit.s3-bucket=xmljson
audit.s3-region=us-east-1
audit.s3-endpoint=http://minio:9000
```

With `audit.backend=s3`, payload bytes are stored in the specified bucket under the given prefix while metadata remains in the database. The UI fetches XML and JSON bodies through short-lived links exposed at `/audit/{id}/xmlUrl` and `/audit/{id}/jsonUrl`.

Audit entries can be searched via `/v1/audit/search?q=TERM&page=0&size=20`, which returns a paginated JSON result and powers the HTML search page.

Environment specific variants of `application.yml` can be placed alongside the default file
using the naming convention `application-{profile}.yml` (e.g. `application-dev.yml`). The active
profile is selected via the standard Spring Boot `spring.profiles.active` property.

### Home Page

The root URL `/` displays links to the audit table and generated OpenAPI documentation.
It also shows the application version, build time and Git commit id extracted from
`build-info.properties` and `git.properties` at runtime. A sample `curl` command is
provided for quickly testing the `/v1/transform` endpoint.

### Feature Flags & Central Config

Runtime behaviour is controlled through properties that can be served by Spring Cloud Config or mounted via a Kubernetes `ConfigMap`.

Defaults are defined in `application.yml`:

```
audit.enabled=true
mapping.pretty-print=false
mapping.escape-non-ascii=false
```

To source settings from a Config Server add:

```
spring:
  application:
    name: xml-json
  config:
    import: "optional:configserver:http://config-server:8888"
```

Refresh properties without restarting:

```
curl -X POST http://localhost:8080/actuator/refresh
```

For Kubernetes deployments, a sample `ConfigMap` manifest is provided in `k8s/configmap.yaml` and mounted in `k8s/deployment.yaml`.

### Observability

The service exposes Prometheus metrics, OpenTelemetry traces and structured JSON logs.

* `/actuator/prometheus` publishes Micrometer metrics such as `transform_duration_seconds`.
* Each response includes an `X-Request-ID` correlation identifier and the active trace id via `X-Trace-Id`.
* Logs are emitted in JSON with correlation and trace information suitable for Loki or ELK.

Set the OTLP endpoint with `OTEL_EXPORTER_OTLP_ENDPOINT` and enable Prometheus scraping by setting `MANAGEMENT_PROMETHEUS_METRICS_EXPORT_ENABLED=true`.

### Security

The service acts as an OAuth2 resource server and requires a valid JWT for `/v1/transform` and audit endpoints. A simple API key mode is available by activating the `apikey` profile and providing an `API_KEY` environment variable. Incoming requests are subject to strict validation and limits:

* Only `application/xml` or `text/xml` content types are accepted and payloads larger than 10&nbsp;MiB are rejected.
* Each client is rate limited to 60 requests per minute.
* CORS origins can be restricted via the `cors.allowed-origins` property.
