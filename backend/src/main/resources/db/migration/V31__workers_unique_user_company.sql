ALTER TABLE workers ADD CONSTRAINT uq_workers_user_company UNIQUE (user_id, company_id);
