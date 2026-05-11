CREATE TABLE subscription_plans (
    code                  VARCHAR(20) PRIMARY KEY,
    name                  VARCHAR(100) NOT NULL,
    max_companies         INT NOT NULL DEFAULT -1,
    max_units             INT NOT NULL DEFAULT -1,
    max_workers           INT NOT NULL DEFAULT -1,
    max_orders_per_month  INT NOT NULL DEFAULT -1,
    max_loyal_users       INT NOT NULL DEFAULT -1
);

INSERT INTO subscription_plans (code, name, max_companies, max_units, max_workers, max_orders_per_month, max_loyal_users) VALUES
    ('FREE',  'Free',   1,  3,   5,  50,   20),
    ('BASIC', 'Basic',  5, 10,  15, 200,  100),
    ('PRO',   'Pro',   -1, -1,  -1,  -1,   -1);
