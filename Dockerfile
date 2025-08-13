# syntax=docker/dockerfile:1.7

# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Leverage Docker build cache
COPY pom.xml .
COPY xml-json-core/pom.xml xml-json-core/pom.xml
COPY xml-json-spring-boot-starter/pom.xml xml-json-spring-boot-starter/pom.xml
RUN mvn -q -e -B -DskipTests dependency:go-offline

COPY . .
RUN mvn -q -e -B -DskipTests package -pl xml-json-spring-boot-starter -am

# Find the bootable jar (adjust artifact name if known)
RUN JAR_FILE=$(ls xml-json-spring-boot-starter/target/*-SNAPSHOT.jar 2>/dev/null || ls xml-json-spring-boot-starter/target/*.jar) && \
    echo "$JAR_FILE" > /jar_path

# ---------- Runtime stage ----------
# Option A: Distroless (most secure)
FROM gcr.io/distroless/java21-debian12:nonroot

# Option B: Alpine JRE (if you need shell/debugging)
# FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /jar_path /app/jar_path
RUN true
COPY --from=build /workspace/xml-json-spring-boot-starter/target /app/target

# Non-root already in distroless:nonroot; otherwise:
# RUN addgroup -S app && adduser -S app -G app
# USER app

# OCI labels
LABEL org.opencontainers.image.title="xml-to-json-transformer-service" \
      org.opencontainers.image.description="Streaming XML->JSON transformer (Spring Boot)" \
      org.opencontainers.image.source="https://github.com/myorg/xml-to-json-transformer-service" \
      org.opencontainers.image.revision="$VCS_REF" \
      org.opencontainers.image.licenses="Apache-2.0"

# Expose port
EXPOSE 8080

# Spring config via env vars; map to Boot properties by relaxed binding.
# Example: AUDIT_BACKEND=file -> audit.backend=file
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC" \
    SERVER_PORT=8080 \
    AUDIT_BACKEND=memory \
    AUDIT_HISTORY_SIZE=100 \
    AUDIT_FILE_PATH=/data/audit-store.json \
    AUDIT_ENABLED=true \
    MAPPING_PRETTY_PRINT=false \
    MAPPING_ESCAPE_NON_ASCII=false

# Optional: provide structured config via SPRING_APPLICATION_JSON
# ENV SPRING_APPLICATION_JSON='{"audit":{"backend":"memory","historySize":100}}'

# Volume for file backend (optional)
VOLUME ["/data"]

# Launch
ENTRYPOINT ["java","-jar","/app/target/xml-json-spring-boot-starter-0.0.1-SNAPSHOT.jar"]
