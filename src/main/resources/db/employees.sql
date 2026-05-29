-- Optional: run if Hibernate ddl-auto does not create tables (MySQL).
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    nic VARCHAR(64) NOT NULL,
    job_start_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    UNIQUE KEY uk_employees_nic (nic)
);

CREATE TABLE IF NOT EXISTS employee_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    payment_type VARCHAR(32) NOT NULL,
    payment_value DECIMAL(12, 2) NOT NULL,
    payment_date DATE NOT NULL,
    CONSTRAINT fk_employee_payments_employee FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE
);
