ALTER TABLE audit_entry ADD COLUMN json_text_excerpt text;
ALTER TABLE audit_entry ADD COLUMN xml_text_excerpt text;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_audit_json_excerpt_trgm ON audit_entry USING gin (json_text_excerpt gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_audit_xml_excerpt_trgm ON audit_entry USING gin (xml_text_excerpt gin_trgm_ops);
