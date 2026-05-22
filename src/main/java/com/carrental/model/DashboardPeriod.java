package com.carrental.model;

import com.carrental.dashboard.DateRange;

import java.time.LocalDate;

public enum DashboardPeriod {
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    THIS_MONTH("This month"),
    LAST_3_MONTHS("Last 3 months"),
    THIS_YEAR("This year"),
    ALL_TIME("All time");

    private final String label;

    DashboardPeriod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public DateRange toDateRange(LocalDate today) {
        switch (this) {
            case LAST_7_DAYS:
                return new DateRange(today.minusDays(6), today);
            case LAST_30_DAYS:
                return new DateRange(today.minusDays(29), today);
            case THIS_MONTH:
                return new DateRange(today.withDayOfMonth(1), today);
            case LAST_3_MONTHS:
                return new DateRange(today.minusMonths(3).plusDays(1), today);
            case THIS_YEAR:
                return new DateRange(today.withDayOfYear(1), today);
            case ALL_TIME:
            default:
                return new DateRange(LocalDate.of(2000, 1, 1), today);
        }
    }

    public enum RevenueGranularity {
        DAILY("Daily revenue"),
        WEEKLY("Weekly revenue"),
        MONTHLY("Monthly revenue");

        private final String chartTitle;

        RevenueGranularity(String chartTitle) {
            this.chartTitle = chartTitle;
        }

        public String getChartTitle() {
            return chartTitle;
        }
    }

    public RevenueGranularity getRevenueGranularity() {
        switch (this) {
            case LAST_7_DAYS:
            case LAST_30_DAYS:
            case THIS_MONTH:
                return RevenueGranularity.DAILY;
            case LAST_3_MONTHS:
                return RevenueGranularity.WEEKLY;
            case THIS_YEAR:
            case ALL_TIME:
            default:
                return RevenueGranularity.MONTHLY;
        }
    }

    public static DashboardPeriod fromParam(String value) {
        if (value == null || value.isEmpty()) {
            return LAST_30_DAYS;
        }
        try {
            return DashboardPeriod.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return LAST_30_DAYS;
        }
    }
}
