package com.carrental.model;

public enum OfficeExpenseCategory {
    RENT("Rent"),
    UTILITIES("Utilities"),
    OFFICE_SUPPLIES("Office supplies"),
    TRANSPORT("Transport"),
    MAINTENANCE("Maintenance"),
    INSURANCE("Insurance"),
    TAXES("Taxes & fees"),
    MARKETING("Marketing"),
    OTHER("Other");

    private final String label;

    OfficeExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
