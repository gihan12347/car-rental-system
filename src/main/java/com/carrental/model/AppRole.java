package com.carrental.model;

public enum AppRole {
    ADMIN("Administrator"),
    USER("Standard user");

    private final String label;

    AppRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
