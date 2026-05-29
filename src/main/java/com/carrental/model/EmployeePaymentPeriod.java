package com.carrental.model;

import com.carrental.dashboard.DateRange;

import java.time.LocalDate;
import java.time.YearMonth;

public enum EmployeePaymentPeriod {
    ALL_TIME("All time"),
    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    LAST_3_MONTHS("Last 3 months"),
    THIS_YEAR("This year");

    private final String label;

    EmployeePaymentPeriod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public DateRange toDateRange(LocalDate today) {
        switch (this) {
            case THIS_MONTH:
                return new DateRange(today.withDayOfMonth(1), today);
            case LAST_MONTH:
                YearMonth last = YearMonth.from(today).minusMonths(1);
                return new DateRange(last.atDay(1), last.atEndOfMonth());
            case LAST_3_MONTHS:
                return new DateRange(today.minusMonths(3).plusDays(1), today);
            case THIS_YEAR:
                return new DateRange(today.withDayOfYear(1), today);
            case ALL_TIME:
            default:
                return new DateRange(LocalDate.of(2000, 1, 1), today);
        }
    }

    public static EmployeePaymentPeriod fromParam(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ALL_TIME;
        }
        try {
            return EmployeePaymentPeriod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ALL_TIME;
        }
    }
}
