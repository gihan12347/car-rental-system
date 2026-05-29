package com.carrental.dashboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateRange {

    private static final DateTimeFormatter LABEL_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

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

    public String formatLabel() {
        return getStart().format(LABEL_FMT) + " – " + getEnd().format(LABEL_FMT);
    }
}
