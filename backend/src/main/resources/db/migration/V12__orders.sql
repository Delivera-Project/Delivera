CREATE SEQUENCE order_ref_seq START 1;

CREATE TYPE order_status AS ENUM ('PENDING', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED');

CREATE TABLE orders (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id     UUID         NOT NULL REFERENCES companies(id),
    reference      VARCHAR(25)  NOT NULL UNIQUE,
    origin_id      UUID         NOT NULL REFERENCES operational_units(id),
    destination_id UUID         NOT NULL REFERENCES operational_units(id),
    status         order_status NOT NULL DEFAULT 'PENDING',
    notes          TEXT,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT chk_different_units CHECK (origin_id <> destination_id)
);

CREATE INDEX idx_orders_company_id ON orders(company_id);
