-- Employee zero-charge hires (optional manual migration if not using ddl-auto=update)
ALTER TABLE rentals
    ADD COLUMN IF NOT EXISTS employee_hire BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE rentals
    ADD COLUMN IF NOT EXISTS employee_id BIGINT NULL;

ALTER TABLE rentals
    ADD CONSTRAINT fk_rentals_employee
        FOREIGN KEY (employee_id) REFERENCES employees (id);
