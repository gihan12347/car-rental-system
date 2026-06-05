-- Rental start/return times (HH:MM) and per-hour overage pricing.

ALTER TABLE cars
    ADD COLUMN extra_price_per_hour DECIMAL(12, 2) NOT NULL DEFAULT 0;

ALTER TABLE rentals
    ADD COLUMN pickup_time TIME NOT NULL DEFAULT '09:00:00';

ALTER TABLE rentals
    ADD COLUMN return_time TIME NULL;

ALTER TABLE rentals
    ADD COLUMN billable_extra_hours DECIMAL(8, 2) NULL;
