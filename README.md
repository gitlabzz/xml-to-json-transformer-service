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

POST XML to `/transform` and receive the mapped JSON. The controller streams the request and response bodies so large documents do not overwhelm memory.

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

`MappingConfig` exposes the following properties which can be overridden via `application.yml`:

```
mapping.attribute-prefix=@
mapping.text-field=#text
mapping.arrays-for-repeated-siblings=true
```

These allow customizing how attributes, text content and repeated elements are represented in the produced JSON.

### Audit History

The service keeps a bounded in-memory history of recent transformations. The history size,
page size for the HTML views and whether the stored payloads are compressed can be configured
using the following properties:

```
audit.history-size=100
audit.page-size=20
audit.compress=true
```

Environment specific variants of `application.yml` can be placed alongside the default file
using the naming convention `application-{profile}.yml` (e.g. `application-dev.yml`). The active
profile is selected via the standard Spring Boot `spring.profiles.active` property.

