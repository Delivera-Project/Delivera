ALTER TABLE companies
    ADD CONSTRAINT fk_companies_activity_type
        FOREIGN KEY (activity_type) REFERENCES activity_types(code);
