package com.carrental.service;

import com.carrental.model.Rental;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class RentalPeriodHelper {

    private RentalPeriodHelper() {
    }

    public static LocalDate startDate(Rental rental) {
        if (rental.getPickupDate() != null) {
            return rental.getPickupDate();
        }
        return rental.getHireDate();
    }

    public static LocalDate endDate(Rental rental) {
        if (rental.getReturnDate() != null) {
            return rental.getReturnDate();
        }
        return startDate(rental).plusDays(Math.max(1, rental.getNumberOfDays()) - 1L);
    }

    public static int inclusiveDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    public static boolean overlaps(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return !startA.isAfter(endB) && !endA.isBefore(startB);
    }

    public static boolean includesToday(LocalDate start, LocalDate end, LocalDate today) {
        return overlaps(start, end, today, today);
    }
}
