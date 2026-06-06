package com.carrental.service;

import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public static LocalTime pickupTime(Rental rental) {
        return rental.getPickupTime() != null ? rental.getPickupTime() : LocalTime.MIDNIGHT;
    }

    public static LocalDateTime pickupDateTime(Rental rental) {
        return RentalDurationHelper.combine(startDate(rental), pickupTime(rental));
    }

    public static LocalDateTime plannedReturnDateTime(Rental rental) {
        LocalDate date = rental.getReturnDate() != null ? rental.getReturnDate() : endDate(rental);
        LocalTime time = rental.getReturnTime() != null ? rental.getReturnTime() : pickupTime(rental);
        return RentalDurationHelper.combine(date, time);
    }

    public static LocalDate endDate(Rental rental) {
        if (rental.getReturnDate() != null) {
            return rental.getReturnDate();
        } else {
            return startDate(rental).plusDays(Math.max(1, rental.getNumberOfDays()) - 1L);
        }
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

    public static boolean isOverdue(Rental rental, LocalDate today) {
        if (rental == null || today == null) {
            return false;
        }
        RentalStatus status = rental.getRentalStatus();
        if (status != RentalStatus.ACTIVE && status != RentalStatus.PENDING) {
            return false;
        }
        LocalDate end = endDate(rental);
        return end != null && end.isBefore(today);
    }

    public static long daysOverdue(Rental rental, LocalDate today) {
        if (!isOverdue(rental, today)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(endDate(rental), today);
    }
}
