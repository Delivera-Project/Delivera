ALTER TABLE operational_units
    ADD CONSTRAINT chk_latitude  CHECK (latitude  BETWEEN -90  AND 90),
    ADD CONSTRAINT chk_longitude CHECK (longitude BETWEEN -180 AND 180);
