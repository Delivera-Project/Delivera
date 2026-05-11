ALTER TABLE companies
    ADD COLUMN plan_code VARCHAR(20) NOT NULL DEFAULT 'FREE'
    REFERENCES subscription_plans(code);
