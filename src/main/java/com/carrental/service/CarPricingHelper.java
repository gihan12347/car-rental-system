package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.model.HireType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Each hire type has its own per-day rate on the vehicle.
 * Total time charge is always: per-day rate (for selected type) × number of days.
 */
public final class CarPricingHelper {

    private CarPricingHelper() {
    }

    public static void normalizeHirePrices(Car car) {
        if (car.getRentalPricePerDay() == null) {
            return;
        }
        BigDecimal day = car.getRentalPricePerDay().setScale(2, RoundingMode.HALF_UP);
        car.setRentalPricePerDay(day);
        if (car.getRentalPricePerWeek() == null) {
            car.setRentalPricePerWeek(day);
        }
        if (car.getRentalPricePerMonth() == null) {
            car.setRentalPricePerMonth(day);
        }
    }

    /** Per-day rate used when the given hire type is selected. */
    public static BigDecimal dailyRateForHireType(Car car, HireType hireType) {
        if (car == null) {
            return BigDecimal.ZERO;
        }
        HireType type = hireType != null ? hireType : HireType.PER_DAY;
        switch (type) {
            case PER_WEEK:
                return resolveWeeklyHireDailyRate(car);
            case PER_MONTH:
                return resolveMonthlyHireDailyRate(car);
            case PER_DAY:
            default:
                return car.getRentalPricePerDay() != null ? car.getRentalPricePerDay() : BigDecimal.ZERO;
        }
    }

    /** Per-day rate for {@link HireType#PER_WEEK} hires (stored in rental_price_per_week). */
    public static BigDecimal resolveWeeklyHireDailyRate(Car car) {
        if (car.getRentalPricePerWeek() != null) {
            return car.getRentalPricePerWeek();
        }
        return car.getRentalPricePerDay() != null ? car.getRentalPricePerDay() : BigDecimal.ZERO;
    }

    /** Per-day rate for {@link HireType#PER_MONTH} hires (stored in rental_price_per_month). */
    public static BigDecimal resolveMonthlyHireDailyRate(Car car) {
        if (car.getRentalPricePerMonth() != null) {
            return car.getRentalPricePerMonth();
        }
        return car.getRentalPricePerDay() != null ? car.getRentalPricePerDay() : BigDecimal.ZERO;
    }

    /** @deprecated use {@link #resolveWeeklyHireDailyRate} */
    public static BigDecimal resolveWeekPrice(Car car) {
        return resolveWeeklyHireDailyRate(car);
    }

    /** @deprecated use {@link #resolveMonthlyHireDailyRate} */
    public static BigDecimal resolveMonthPrice(Car car) {
        return resolveMonthlyHireDailyRate(car);
    }
}
