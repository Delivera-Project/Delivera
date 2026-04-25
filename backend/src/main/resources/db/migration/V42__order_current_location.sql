ALTER TABLE orders
    ADD COLUMN current_lat NUMERIC(9, 6),
    ADD COLUMN current_lon NUMERIC(9, 6),
    ADD COLUMN current_location_at TIMESTAMP;
