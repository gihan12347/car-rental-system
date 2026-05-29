-- Optional: run if Hibernate ddl-auto does not create tables (MySQL).
CREATE TABLE IF NOT EXISTS office_expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(32) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    expense_date DATE NOT NULL,
    reference_number VARCHAR(64) NULL,
    notes VARCHAR(1000) NULL
);

-- If upgrading from a version with title/vendor columns:
-- ALTER TABLE office_expenses DROP COLUMN title;
-- ALTER TABLE office_expenses DROP COLUMN vendor;
