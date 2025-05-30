# XML to JSON Transformer

This project provides a Spring Boot service that converts arbitrary XML documents to JSON without loading the entire document in memory. It streams the XML using StAX and writes JSON directly via Jackson.

## Usage

Run tests:

```bash
mvn clean test
```

Start the service (optional):

```bash
mvn spring-boot:run
```

POST XML to `/transform` and receive the mapped JSON.

## Mapping Rules

* Elements become object keys using their qualified name.
* Attributes are prefixed with `@`.
* Mixed content uses `#text` for the text value alongside children.
* Repeated sibling elements are emitted as arrays.
* Comments and processing instructions are ignored.
* Namespace prefixes are preserved.
* Values remain strings.

