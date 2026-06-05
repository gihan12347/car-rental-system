package com.carrental.service;

import com.carrental.model.Rental;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RentalDisplayHelper {

    private static final DateTimeFormatter TIME_AM_PM =
            DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    private RentalDisplayHelper() {
    }

    public static String formatTimeAmPm(LocalTime time) {
        if (time == null) {
            return "—";
        }
        return time.format(TIME_AM_PM);
    }

    public static String formatTimeAmPm(LocalTime time, LocalTime fallback) {
        LocalTime value = time != null ? time : fallback;
        return formatTimeAmPm(value);
    }

    public static String formatDuration(Rental rental) {
        if (rental == null) {
            return "—";
        }
        return formatDurationParts(rental.getNumberOfDays(), rental.getBillableExtraHours());
    }

    public static String formatDurationParts(Integer days, BigDecimal extraHours) {
        int dayCount = days != null ? days : 0;
        List<String> parts = new ArrayList<>();
        if (dayCount > 0) {
            parts.add(dayCount + (dayCount == 1 ? " day" : " days"));
        }
        if (extraHours != null && extraHours.compareTo(BigDecimal.ZERO) > 0) {
            parts.add(extraHours.stripTrailingZeros().toPlainString() + " hr");
        }
        if (parts.isEmpty()) {
            return "under 1 hr";
        }
        return String.join(" + ", parts);
    }
}
