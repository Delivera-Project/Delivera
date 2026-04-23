CREATE TABLE api_keys (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    prefix      VARCHAR(16)  NOT NULL,
    key_hash    VARCHAR(128) NOT NULL UNIQUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at  TIMESTAMP,
    last_used_at TIMESTAMP
);

CREATE INDEX idx_api_keys_company ON api_keys(company_id);
CREATE INDEX idx_api_keys_prefix  ON api_keys(prefix);
