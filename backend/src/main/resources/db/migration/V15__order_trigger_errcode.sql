CREATE OR REPLACE FUNCTION validate_order_operational_units_company()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM operational_units
        WHERE id = NEW.origin_id AND company_id = NEW.company_id
    ) THEN
        RAISE EXCEPTION 'origin_id does not belong to company %', NEW.company_id
            USING ERRCODE = '23514';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM operational_units
        WHERE id = NEW.destination_id AND company_id = NEW.company_id
    ) THEN
        RAISE EXCEPTION 'destination_id does not belong to company %', NEW.company_id
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$;
