-- Per-day rates for each hire type (daily / weekly / monthly hire).
-- Time charge is always: selected per-day rate × number of rental days.
-- Column names rental_price_per_week/month store the per-day rate for that hire type.

ALTER TABLE cars
    ADD COLUMN rental_price_per_week DECIMAL(12, 2) NULL AFTER rental_price_per_day,
    ADD COLUMN rental_price_per_month DECIMAL(12, 2) NULL AFTER rental_price_per_week;

UPDATE cars
SET rental_price_per_week = rental_price_per_day
WHERE rental_price_per_week IS NULL;

UPDATE cars
SET rental_price_per_month = rental_price_per_day
WHERE rental_price_per_month IS NULL;

ALTER TABLE cars
    MODIFY rental_price_per_week DECIMAL(12, 2) NOT NULL,
    MODIFY rental_price_per_month DECIMAL(12, 2) NOT NULL;

ALTER TABLE rentals
    ADD COLUMN hire_type VARCHAR(32) NOT NULL DEFAULT 'PER_DAY' AFTER number_of_days;

    UPDATE cars
    SET rental_price_per_week = rental_price_per_day,
        rental_price_per_month = rental_price_per_day;

-- If you previously ran an older script that set week/month to day×7 or day×30, reset to per-day rates:
-- UPDATE cars SET rental_price_per_week = rental_price_per_day, rental_price_per_month = rental_price_per_day;
