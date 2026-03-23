-- Permitir pedidos externos (sin unidad destino)
ALTER TABLE orders ALTER COLUMN destination_id DROP NOT NULL;

-- Añadir campos para pedidos externos y prioridad
ALTER TABLE orders
    ADD COLUMN priority       VARCHAR(10)  NOT NULL DEFAULT 'NORMAL',
    ADD COLUMN tracking_token VARCHAR(64)  UNIQUE,
    ADD COLUMN recipient_email VARCHAR(255),
    ADD COLUMN recipient_name  VARCHAR(255);

-- Actualizar trigger: solo valida destination si no es NULL
CREATE OR REPLACE FUNCTION validate_order_operational_units_company()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM operational_units
        WHERE id = NEW.origin_id AND company_id = NEW.company_id
    ) THEN
        RAISE EXCEPTION 'origin_id does not belong to company %', NEW.company_id
            USING ERRCODE = '23514';
    END IF;

    IF NEW.destination_id IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM operational_units
        WHERE id = NEW.destination_id AND company_id = NEW.company_id
    ) THEN
        RAISE EXCEPTION 'destination_id does not belong to company %', NEW.company_id
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;

-- Historial de cambios de estado
CREATE TABLE order_events (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID         NOT NULL REFERENCES orders(id),
    status       VARCHAR(20)  NOT NULL,
    note         TEXT,
    author_email VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_events_order_id ON order_events(order_id);
