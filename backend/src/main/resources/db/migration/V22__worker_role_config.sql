CREATE TABLE worker_role_config (
    role                    VARCHAR(50) PRIMARY KEY,
    can_create_orders       BOOLEAN NOT NULL DEFAULT FALSE,
    can_update_order_status BOOLEAN NOT NULL DEFAULT FALSE,
    can_manage_units        BOOLEAN NOT NULL DEFAULT FALSE,
    can_manage_loyal_users  BOOLEAN NOT NULL DEFAULT FALSE,
    can_manage_settings     BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO worker_role_config (role, can_create_orders, can_update_order_status, can_manage_units, can_manage_loyal_users, can_manage_settings) VALUES
    ('COMPANY_ADMIN', TRUE,  TRUE,  TRUE,  TRUE,  TRUE),
    ('ANALYST',       TRUE,  TRUE,  FALSE, TRUE,  FALSE),
    ('OPERATOR',      FALSE, TRUE,  FALSE, FALSE, FALSE);
