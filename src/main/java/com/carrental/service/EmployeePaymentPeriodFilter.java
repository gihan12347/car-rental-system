package com.carrental.service;

import com.carrental.dashboard.DateRange;
import com.carrental.model.EmployeePaymentPeriod;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class EmployeePaymentPeriodFilter {

    public static final int PAYMENT_PAGE_SIZE = 10;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private final EmployeePaymentPeriod period;
    private final LocalDate from;
    private final LocalDate to;
    private final DateRange range;
    private final String rangeLabel;

    private EmployeePaymentPeriodFilter(
            EmployeePaymentPeriod period,
            LocalDate from,
            LocalDate to,
            DateRange range,
            String rangeLabel) {
        this.period = period;
        this.from = from;
        this.to = to;
        this.range = range;
        this.rangeLabel = rangeLabel;
    }

    public static EmployeePaymentPeriodFilter fromDateRange(DateRange range, String rangeLabel) {
        return new EmployeePaymentPeriodFilter(
                EmployeePaymentPeriod.ALL_TIME,
                range.getStart(),
                range.getEnd(),
                range,
                rangeLabel);
    }

    public static EmployeePaymentPeriodFilter resolve(
            String periodParam,
            LocalDate from,
            LocalDate to,
            LocalDate today) {
        if (from != null && to != null && !to.isBefore(from)) {
            DateRange custom = new DateRange(from, to);
            return new EmployeePaymentPeriodFilter(
                    EmployeePaymentPeriod.ALL_TIME,
                    from,
                    to,
                    custom,
                    custom.getStart().format(DAY_FMT) + " – " + custom.getEnd().format(DAY_FMT));
        }
        EmployeePaymentPeriod period = EmployeePaymentPeriod.fromParam(periodParam);
        DateRange range = period.toDateRange(today);
        return new EmployeePaymentPeriodFilter(period, null, null, range, period.getLabel());
    }

    public EmployeePaymentPeriod getPeriod() {
        return period;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public DateRange getRange() {
        return range;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public String getPeriodParam() {
        return period.name();
    }
}
