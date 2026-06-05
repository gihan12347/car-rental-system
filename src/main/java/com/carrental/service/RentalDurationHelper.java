package com.carrental.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Billing periods are 24 hours from pickup date/time.
 * Full days = total duration ÷ 24h (integer); extra hours = remainder.
 */
public final class RentalDurationHelper {

    private static final int MINUTES_PER_DAY = 24 * 60;

    private RentalDurationHelper() {
    }

    public static Split split24h(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end date/time are required.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End date/time must be after the start date/time.");
        }
        long totalMinutes = Duration.between(start, end).toMinutes();
        int fullDays = (int) (totalMinutes / MINUTES_PER_DAY);
        long remainderMinutes = totalMinutes % MINUTES_PER_DAY;
        BigDecimal extraHours = BigDecimal.valueOf(remainderMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return new Split(fullDays, extraHours, totalMinutes);
    }

    public static LocalDateTime combine(java.time.LocalDate date, LocalTime time) {
        if (date == null) {
            return null;
        }
        LocalTime resolved = time != null ? time : LocalTime.MIDNIGHT;
        return LocalDateTime.of(date, resolved);
    }

    public static final class Split {
        private final int fullDays;
        private final BigDecimal extraHours;
        private final long totalMinutes;

        public Split(int fullDays, BigDecimal extraHours, long totalMinutes) {
            this.fullDays = fullDays;
            this.extraHours = extraHours;
            this.totalMinutes = totalMinutes;
        }

        public int getFullDays() {
            return fullDays;
        }

        public BigDecimal getExtraHours() {
            return extraHours;
        }

        public long getTotalMinutes() {
            return totalMinutes;
        }
    }
}
