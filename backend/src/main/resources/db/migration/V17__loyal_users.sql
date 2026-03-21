CREATE TABLE loyal_users (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID         NOT NULL REFERENCES companies(id),
    email      VARCHAR(255) NOT NULL,
    user_id    UUID         REFERENCES users(id),
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_loyal_user_company_email UNIQUE (company_id, email)
);

CREATE INDEX idx_loyal_users_company_id ON loyal_users(company_id);
CREATE INDEX idx_loyal_users_user_id    ON loyal_users(user_id);
CREATE INDEX idx_loyal_users_email      ON loyal_users(email);

ALTER TABLE orders ADD COLUMN loyal_user_id UUID REFERENCES loyal_users(id);
