package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.model.HireType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class RentalPricingHelper {

    private RentalPricingHelper() {
    }

    public static PriceBreakdown calculate(
            Car car,
            HireType hireType,
            LocalDate pickupDate,
            LocalDate returnDate,
            int returnMileageKm) {
        return calculateInternal(car, resolveHireType(hireType), pickupDate, returnDate, returnMileageKm);
    }

    public static PriceBreakdown calculate(
            Car car,
            LocalDate pickupDate,
            LocalDate returnDate,
            int returnMileageKm) {
        return calculate(car, HireType.PER_DAY, pickupDate, returnDate, returnMileageKm);
    }

    /** Employee vehicle hire — records trip details but charges nothing. */
    public static PriceBreakdown calculateWaived(
            Car car,
            HireType hireType,
            LocalDate pickupDate,
            LocalDate returnDate,
            int returnMileageKm) {
        PriceBreakdown breakdown = calculateInternal(car, resolveHireType(hireType), pickupDate, returnDate, returnMileageKm);
        return zeroCharges(breakdown);
    }

    public static PriceBreakdown calculateWaived(
            Car car,
            LocalDate pickupDate,
            LocalDate returnDate,
            int returnMileageKm) {
        return calculateWaived(car, HireType.PER_DAY, pickupDate, returnDate, returnMileageKm);
    }

    /** Per-day rate for this hire type (changes with type; charge is always rate × days). */
    public static BigDecimal effectiveDailyRate(Car car, HireType hireType) {
        return CarPricingHelper.dailyRateForHireType(car, resolveHireType(hireType));
    }

    public static BigDecimal computeDailyCharge(Car car, HireType hireType, int days) {
        if (days <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return effectiveDailyRate(car, hireType)
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static String dailyChargeFormula(Car car, HireType hireType, int days) {
        BigDecimal rate = effectiveDailyRate(car, hireType);
        String dayLabel = days == 1 ? "day" : "days";
        return rate + " / day × " + days + " " + dayLabel;
    }

    private static PriceBreakdown calculateInternal(
            Car car,
            HireType hireType,
            LocalDate pickupDate,
            LocalDate returnDate,
            int returnMileageKm) {
        if (car == null) {
            throw new IllegalArgumentException("Vehicle not found for this rental.");
        }
        if (pickupDate == null || returnDate == null) {
            throw new IllegalArgumentException("Pickup and return dates are required.");
        }
        if (returnDate.isBefore(pickupDate)) {
            throw new IllegalArgumentException("Return date cannot be before the rental start date.");
        }
        int startMileage = car.getMileageKm() != null ? car.getMileageKm() : 0;
        if (returnMileageKm < startMileage) {
            throw new IllegalArgumentException(
                    "Return mileage (" + returnMileageKm + " km) cannot be less than the vehicle odometer at handover ("
                            + startMileage + " km).");
        }

        int days = RentalPeriodHelper.inclusiveDays(pickupDate, returnDate);
        int tripKm = returnMileageKm - startMileage;
        int freeKmPerDay = car.getFreeKmPerDay() != null ? Math.max(0, car.getFreeKmPerDay()) : 0;
        int includedKm = freeKmPerDay * days;
        int billableExtraKm = Math.max(0, tripKm - includedKm);

        BigDecimal dailyRate = effectiveDailyRate(car, hireType);
        BigDecimal kmRate = car.getExtraPricePerKm() != null ? car.getExtraPricePerKm() : BigDecimal.ZERO;
        BigDecimal dailyCharge = computeDailyCharge(car, hireType, days);
        BigDecimal kmCharge = kmRate.multiply(BigDecimal.valueOf(billableExtraKm)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = dailyCharge.add(kmCharge).setScale(2, RoundingMode.HALF_UP);

        return new PriceBreakdown(
                hireType,
                startMileage,
                returnMileageKm,
                tripKm,
                days,
                freeKmPerDay,
                includedKm,
                billableExtraKm,
                dailyRate,
                kmRate,
                dailyCharge,
                kmCharge,
                total);
    }

    private static PriceBreakdown zeroCharges(PriceBreakdown breakdown) {
        return new PriceBreakdown(
                breakdown.getHireType(),
                breakdown.getStartMileageKm(),
                breakdown.getReturnMileageKm(),
                breakdown.getTripKm(),
                breakdown.getDays(),
                breakdown.getFreeKmPerDay(),
                breakdown.getIncludedKm(),
                breakdown.getBillableExtraKm(),
                breakdown.getDailyRate(),
                breakdown.getKmRate(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO);
    }

    private static HireType resolveHireType(HireType hireType) {
        return hireType != null ? hireType : HireType.PER_DAY;
    }

    public static class PriceBreakdown {
        private final HireType hireType;
        private final int startMileageKm;
        private final int returnMileageKm;
        private final int tripKm;
        private final int days;
        private final int freeKmPerDay;
        private final int includedKm;
        private final int billableExtraKm;
        private final BigDecimal dailyRate;
        private final BigDecimal kmRate;
        private final BigDecimal dailyCharge;
        private final BigDecimal kmCharge;
        private final BigDecimal total;

        public PriceBreakdown(
                HireType hireType,
                int startMileageKm,
                int returnMileageKm,
                int tripKm,
                int days,
                int freeKmPerDay,
                int includedKm,
                int billableExtraKm,
                BigDecimal dailyRate,
                BigDecimal kmRate,
                BigDecimal dailyCharge,
                BigDecimal kmCharge,
                BigDecimal total) {
            this.hireType = hireType;
            this.startMileageKm = startMileageKm;
            this.returnMileageKm = returnMileageKm;
            this.tripKm = tripKm;
            this.days = days;
            this.freeKmPerDay = freeKmPerDay;
            this.includedKm = includedKm;
            this.billableExtraKm = billableExtraKm;
            this.dailyRate = dailyRate;
            this.kmRate = kmRate;
            this.dailyCharge = dailyCharge;
            this.kmCharge = kmCharge;
            this.total = total;
        }

        public HireType getHireType() {
            return hireType;
        }

        public int getStartMileageKm() {
            return startMileageKm;
        }

        public int getReturnMileageKm() {
            return returnMileageKm;
        }

        public int getTripKm() {
            return tripKm;
        }

        public int getDays() {
            return days;
        }

        public int getFreeKmPerDay() {
            return freeKmPerDay;
        }

        public int getIncludedKm() {
            return includedKm;
        }

        public int getBillableExtraKm() {
            return billableExtraKm;
        }

        public BigDecimal getDailyRate() {
            return dailyRate;
        }

        public BigDecimal getKmRate() {
            return kmRate;
        }

        public BigDecimal getDailyCharge() {
            return dailyCharge;
        }

        public BigDecimal getKmCharge() {
            return kmCharge;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }
}
