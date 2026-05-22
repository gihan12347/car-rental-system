-- Free km included per rental day (MySQL 8+)
-- Run against database: car-rental-system
-- Skip if column already exists (Hibernate ddl-auto=update may create it automatically)

ALTER TABLE cars
    ADD COLUMN free_km_per_day INT NOT NULL DEFAULT 0
        COMMENT 'Kilometres included per rental day before extra km rate applies';

-- Example: give all existing cars 100 free km per day
-- UPDATE cars SET free_km_per_day = 100;
