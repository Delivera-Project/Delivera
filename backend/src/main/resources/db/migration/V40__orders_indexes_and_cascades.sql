-- Índices en columnas frecuentemente filtradas (listados y panel de actividad)
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_priority ON orders(priority);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_events_created_at ON order_events(created_at);

-- Política ON DELETE explícita en FKs antiguas (V12/V16 las dejaban a NO ACTION por defecto)
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_company_id_fkey;
ALTER TABLE orders ADD CONSTRAINT orders_company_id_fkey
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_origin_id_fkey;
ALTER TABLE orders ADD CONSTRAINT orders_origin_id_fkey
    FOREIGN KEY (origin_id) REFERENCES operational_units(id) ON DELETE RESTRICT;

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_destination_id_fkey;
ALTER TABLE orders ADD CONSTRAINT orders_destination_id_fkey
    FOREIGN KEY (destination_id) REFERENCES operational_units(id) ON DELETE RESTRICT;

ALTER TABLE order_events DROP CONSTRAINT IF EXISTS order_events_order_id_fkey;
ALTER TABLE order_events ADD CONSTRAINT order_events_order_id_fkey
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;
