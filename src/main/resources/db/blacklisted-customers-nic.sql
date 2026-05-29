-- Optional: migrate blacklist from phone to NIC (MySQL).
-- With Hibernate ddl-auto=update, customer_id_number is added automatically.
-- Run manually if you need to preserve existing blacklist rows keyed by phone.

-- ALTER TABLE blacklisted_customers ADD COLUMN customer_id_number VARCHAR(64) NULL;
-- UPDATE blacklisted_customers SET customer_id_number = UPPER(REPLACE(customer_contact, ' ', '')) WHERE customer_id_number IS NULL;
-- ALTER TABLE blacklisted_customers MODIFY customer_id_number VARCHAR(64) NOT NULL;
-- ALTER TABLE blacklisted_customers ADD UNIQUE KEY uk_blacklisted_nic (customer_id_number);
-- ALTER TABLE blacklisted_customers DROP COLUMN customer_contact;

CREATE TABLE IF NOT EXISTS blacklisted_customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id_number VARCHAR(64) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    reason VARCHAR(500) NULL,
    UNIQUE KEY uk_blacklisted_nic (customer_id_number)
);
