-- Optional discount and comment when completing a rental.
-- total_price = calculated subtotal - completion_discount

ALTER TABLE rentals
    ADD COLUMN completion_discount DECIMAL(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE rentals
    ADD COLUMN completion_comment VARCHAR(1000) NULL;
