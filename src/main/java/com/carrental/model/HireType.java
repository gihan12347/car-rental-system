package com.carrental.model;

/**
 * How the vehicle rental rate is applied for a booking.
 */
public enum HireType {
    PER_DAY("Daily hire"),
    PER_WEEK("Weekly hire"),
    PER_MONTH("Monthly hire");

    private final String label;

    HireType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
