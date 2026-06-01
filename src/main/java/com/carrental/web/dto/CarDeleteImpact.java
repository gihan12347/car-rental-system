package com.carrental.web.dto;

/**
 * Counts of records that will be removed when a vehicle is deleted.
 */
public final class CarDeleteImpact {

    private final long rentalCount;
    private final long maintenanceCount;

    public CarDeleteImpact(long rentalCount, long maintenanceCount) {
        this.rentalCount = rentalCount;
        this.maintenanceCount = maintenanceCount;
    }

    public long getRentalCount() {
        return rentalCount;
    }

    public long getMaintenanceCount() {
        return maintenanceCount;
    }
}
