ALTER TABLE users ADD COLUMN first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN last_name VARCHAR(100);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);

UPDATE users SET first_name = 'Admin', last_name = 'Delivera', phone = '+34600000001'
WHERE email = 'admin@delivera.com';

UPDATE users SET first_name = 'Usuario', last_name = 'Demo', phone = '+34600000002'
WHERE email = 'usuario@delivera.com';
