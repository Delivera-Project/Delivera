ALTER TABLE loyal_users DROP CONSTRAINT uq_loyal_user_company_email;

CREATE TABLE loyal_user_companies (
    loyal_user_id UUID NOT NULL REFERENCES loyal_users(id) ON DELETE CASCADE,
    company_id    UUID NOT NULL REFERENCES companies(id)   ON DELETE CASCADE,
    PRIMARY KEY (loyal_user_id, company_id)
);

INSERT INTO loyal_user_companies (loyal_user_id, company_id)
SELECT id, company_id FROM loyal_users;

ALTER TABLE loyal_users DROP COLUMN company_id;

ALTER TABLE loyal_users ADD CONSTRAINT uq_loyal_user_email UNIQUE (email);

CREATE INDEX idx_loyal_user_companies_company_id ON loyal_user_companies(company_id);
