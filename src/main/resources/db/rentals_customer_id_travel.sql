-- Run once if Hibernate ddl-auto does not add columns (existing MySQL database).
ALTER TABLE rentals
    ADD COLUMN customer_id_number VARCHAR(64) NOT NULL DEFAULT '' AFTER customer_contact,
    ADD COLUMN travel_location VARCHAR(500) NOT NULL DEFAULT '' AFTER customer_id_number;

UPDATE rentals SET customer_id_number = '942940823V' WHERE customer_id_number = '';
UPDATE rentals SET travel_location = 'N/A' WHERE travel_location = '';
