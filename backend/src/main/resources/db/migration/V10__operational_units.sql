CREATE TYPE unit_type AS ENUM ('WAREHOUSE', 'STORE', 'FACTORY', 'LOGISTICS_CENTER');

CREATE TABLE operational_units (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID         NOT NULL REFERENCES companies(id),
    name       VARCHAR(255) NOT NULL,
    type       unit_type    NOT NULL,
    address    VARCHAR(500),
    latitude   DECIMAL(9,6),
    longitude  DECIMAL(9,6),
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (company_id, name)
);

CREATE INDEX idx_operational_units_company_id ON operational_units(company_id);
