-- Dirección y coordenadas del usuario registrado (perfil)
ALTER TABLE users
    ADD COLUMN address   VARCHAR(500),
    ADD COLUMN latitude  DECIMAL(9,6),
    ADD COLUMN longitude DECIMAL(9,6);

-- Dirección y coordenadas del fidelizado (editable por la empresa)
ALTER TABLE loyal_users
    ADD COLUMN address   VARCHAR(500),
    ADD COLUMN latitude  DECIMAL(9,6),
    ADD COLUMN longitude DECIMAL(9,6);

-- Snapshot de la dirección del destinatario en el pedido B2C
ALTER TABLE orders
    ADD COLUMN recipient_address   VARCHAR(500),
    ADD COLUMN recipient_latitude  DECIMAL(9,6),
    ADD COLUMN recipient_longitude DECIMAL(9,6);
