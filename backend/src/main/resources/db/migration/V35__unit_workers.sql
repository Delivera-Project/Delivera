CREATE TABLE unit_workers (
    unit_id   UUID NOT NULL REFERENCES operational_units(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES workers(id) ON DELETE CASCADE,
    PRIMARY KEY (unit_id, worker_id)
);
