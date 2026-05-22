package com.carrental.dashboard;

import java.time.LocalDate;

public class DateRange {

    private final LocalDate start;
    private final LocalDate end;

    public DateRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(start) && !date.isAfter(end);
    }

    public long dayCount() {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    }
}
