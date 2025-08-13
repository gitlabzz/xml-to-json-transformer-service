CREATE TABLE IF NOT EXISTS audit_entry (
  id           BIGSERIAL PRIMARY KEY,
  client_ip    VARCHAR(64) NOT NULL,
  request_time BIGINT NOT NULL,
  response_time BIGINT NOT NULL,
  success      BOOLEAN NOT NULL,
  duration_ms  BIGINT NOT NULL,
  xml_data     BYTEA NOT NULL,
  json_data    BYTEA NOT NULL,
  compressed   BOOLEAN NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_request_time ON audit_entry(request_time);
CREATE INDEX IF NOT EXISTS idx_audit_success ON audit_entry(success);
