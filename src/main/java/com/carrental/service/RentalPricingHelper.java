package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.model.HireType;
import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class RentalPricingHelper {

    private RentalPricingHelper() {
    }

    public static PriceBreakdown calculate(
            Car car,
            HireType hireType,
            LocalDateTime pickupDateTime,
            LocalDateTime returnDateTime,
            int returnMileageKm) {
        return calculateInternal(car, resolveHireType(hireType), pickupDateTime, returnDateTime, returnMileageKm);
    }

    /**
     * Completion pricing: if actual return is after the booked end, bill through actual return;
     * otherwise charge the booked (pending) period only — even when returned early.
     */
    public static PriceBreakdown calculateForCompletion(
            Car car,
            HireType hireType,
            LocalDateTime pickupDateTime,
            LocalDateTime plannedReturnDateTime,
            LocalDateTime actualReturnDateTime,
            int returnMileageKm) {
        LocalDateTime billingEnd = resolveBillingEnd(plannedReturnDateTime, actualReturnDateTime);
        return calculateInternal(
                car, resolveHireType(hireType), pickupDateTime, billingEnd, returnMileageKm);
    }

    public static PriceBreakdown calculateWaivedForCompletion(
            Car car,
            HireType hireType,
            LocalDateTime pickupDateTime,
            LocalDateTime plannedReturnDateTime,
            LocalDateTime actualReturnDateTime,
            int returnMileageKm) {
        PriceBreakdown breakdown = calculateForCompletion(
                car, hireType, pickupDateTime, plannedReturnDateTime, actualReturnDateTime, returnMileageKm);
        return zeroCharges(breakdown);
    }

    public static LocalDateTime resolveBillingEnd(
            LocalDateTime plannedReturnDateTime,
            LocalDateTime actualReturnDateTime) {
        if (plannedReturnDateTime == null) {
            return actualReturnDateTime;
        }
        if (actualReturnDateTime == null) {
            return plannedReturnDateTime;
        }
        return actualReturnDateTime.isAfter(plannedReturnDateTime)
                ? actualReturnDateTime
                : plannedReturnDateTime;
    }

    public static boolean usesPlannedPeriodPrice(
            LocalDateTime plannedReturnDateTime,
            LocalDateTime actualReturnDateTime) {
        if (plannedReturnDateTime == null || actualReturnDateTime == null) {
            return false;
        }
        return !actualReturnDateTime.isAfter(plannedReturnDateTime);
    }

    public static PriceBreakdown calculate(
            Car car,
            LocalDateTime pickupDateTime,
            LocalDateTime returnDateTime,
            int returnMileageKm) {
        return calculate(car, HireType.PER_DAY, pickupDateTime, returnDateTime, returnMileageKm);
    }

    /** @deprecated use {@link #calculate(Car, HireType, LocalDateTime, LocalDateTime, int)} */
    @Deprecated
    public static PriceBreakdown calculate(
            Car car,
            HireType hireType,
            LocalDate pickupDate,
            LocalDate returnDate,
            int returnMileageKm) {
        return calculate(
                car,
                hireType,
                RentalDurationHelper.combine(pickupDate, null),
                RentalDurationHelper.combine(returnDate, null),
                returnMileageKm);
    }

    public static PriceBreakdown calculateWaived(
            Car car,
            HireType hireType,
            LocalDateTime pickupDateTime,
            LocalDateTime returnDateTime,
            int returnMileageKm) {
        PriceBreakdown breakdown = calculateInternal(
                car, resolveHireType(hireType), pickupDateTime, returnDateTime, returnMileageKm);
        return zeroCharges(breakdown);
    }

    public static PriceBreakdown calculateWaived(
            Car car,
            HireType hireType,
            LocalDate pickupDate,
            LocalDate returnDate,
            int returnMileageKm) {
        return calculateWaived(
                car,
                hireType,
                RentalDurationHelper.combine(pickupDate, null),
                RentalDurationHelper.combine(returnDate, null),
                returnMileageKm);
    }

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

    public static BigDecimal computeHourCharge(Car car, BigDecimal extraHours) {
        if (extraHours == null || extraHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal hourRate = car.getExtraPricePerHour() != null ? car.getExtraPricePerHour() : BigDecimal.ZERO;
        return hourRate.multiply(extraHours).setScale(2, RoundingMode.HALF_UP);
    }

    public static String dailyChargeFormula(Car car, HireType hireType, int days) {
        BigDecimal rate = effectiveDailyRate(car, hireType);
        String dayLabel = days == 1 ? "day" : "days";
        return rate + " / day × " + days + " " + dayLabel;
    }

    public static String hourChargeFormula(Car car, BigDecimal extraHours) {
        BigDecimal hourRate = car.getExtraPricePerHour() != null ? car.getExtraPricePerHour() : BigDecimal.ZERO;
        return hourRate + " / hr × " + extraHours + " hr";
    }

    /**
     * Price shown on rental lists: final total when completed, otherwise estimated time charge
     * (daily + extra hours; km not included until completion).
     */
    public static BigDecimal resolveDisplayHirePrice(Rental rental) {
        if (rental == null || rental.getCar() == null) {
            return null;
        }
        if (Boolean.TRUE.equals(rental.getEmployeeHire())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        RentalStatus status = rental.getRentalStatus();
        if (status == RentalStatus.COMPLETED && rental.getTotalPrice() != null) {
            return rental.getTotalPrice();
        }
        if (status == RentalStatus.CANCELLED) {
            return null;
        }
        LocalDateTime pickup = RentalPeriodHelper.pickupDateTime(rental);
        LocalDateTime plannedReturn = RentalPeriodHelper.plannedReturnDateTime(rental);
        if (pickup == null || plannedReturn == null || !plannedReturn.isAfter(pickup)) {
            return null;
        }
        try {
            RentalDurationHelper.Split duration = RentalDurationHelper.split24h(pickup, plannedReturn);
            HireType hireType = resolveHireType(rental.getHireType());
            BigDecimal daily = computeDailyCharge(rental.getCar(), hireType, duration.getFullDays());
            BigDecimal hour = computeHourCharge(rental.getCar(), duration.getExtraHours());
            return daily.add(hour).setScale(2, RoundingMode.HALF_UP);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static boolean isEstimatedHirePrice(Rental rental) {
        if (rental == null || Boolean.TRUE.equals(rental.getEmployeeHire())) {
            return false;
        }
        RentalStatus status = rental.getRentalStatus();
        return status != RentalStatus.COMPLETED && status != RentalStatus.CANCELLED;
    }

    private static PriceBreakdown calculateInternal(
            Car car,
            HireType hireType,
            LocalDateTime pickupDateTime,
            LocalDateTime returnDateTime,
            int returnMileageKm) {
        if (car == null) {
            throw new IllegalArgumentException("Vehicle not found for this rental.");
        }
        if (pickupDateTime == null || returnDateTime == null) {
            throw new IllegalArgumentException("Pickup and return date/time are required.");
        }

        RentalDurationHelper.Split duration = RentalDurationHelper.split24h(pickupDateTime, returnDateTime);
        int days = duration.getFullDays();
        BigDecimal extraHours = duration.getExtraHours();

        int startMileage = car.getMileageKm() != null ? car.getMileageKm() : 0;
        if (returnMileageKm < startMileage) {
            throw new IllegalArgumentException(
                    "Return mileage (" + returnMileageKm + " km) cannot be less than the vehicle odometer at handover ("
                            + startMileage + " km).");
        }

        int tripKm = returnMileageKm - startMileage;
        int freeKmPerDay = car.getFreeKmPerDay() != null ? Math.max(0, car.getFreeKmPerDay()) : 0;
        int includedKm = freeKmPerDay * days;
        int billableExtraKm = Math.max(0, tripKm - includedKm);

        BigDecimal dailyRate = effectiveDailyRate(car, hireType);
        BigDecimal hourRate = car.getExtraPricePerHour() != null ? car.getExtraPricePerHour() : BigDecimal.ZERO;
        BigDecimal kmRate = car.getExtraPricePerKm() != null ? car.getExtraPricePerKm() : BigDecimal.ZERO;
        BigDecimal dailyCharge = computeDailyCharge(car, hireType, days);
        BigDecimal hourCharge = computeHourCharge(car, extraHours);
        BigDecimal kmCharge = kmRate.multiply(BigDecimal.valueOf(billableExtraKm)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = dailyCharge.add(hourCharge).add(kmCharge).setScale(2, RoundingMode.HALF_UP);

        return new PriceBreakdown(
                hireType,
                startMileage,
                returnMileageKm,
                tripKm,
                days,
                extraHours,
                freeKmPerDay,
                includedKm,
                billableExtraKm,
                dailyRate,
                hourRate,
                kmRate,
                dailyCharge,
                hourCharge,
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
                breakdown.getExtraHours(),
                breakdown.getFreeKmPerDay(),
                breakdown.getIncludedKm(),
                breakdown.getBillableExtraKm(),
                breakdown.getDailyRate(),
                breakdown.getHourRate(),
                breakdown.getKmRate(),
                BigDecimal.ZERO,
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
        private final BigDecimal extraHours;
        private final int freeKmPerDay;
        private final int includedKm;
        private final int billableExtraKm;
        private final BigDecimal dailyRate;
        private final BigDecimal hourRate;
        private final BigDecimal kmRate;
        private final BigDecimal dailyCharge;
        private final BigDecimal hourCharge;
        private final BigDecimal kmCharge;
        private final BigDecimal total;

        public PriceBreakdown(
                HireType hireType,
                int startMileageKm,
                int returnMileageKm,
                int tripKm,
                int days,
                BigDecimal extraHours,
                int freeKmPerDay,
                int includedKm,
                int billableExtraKm,
                BigDecimal dailyRate,
                BigDecimal hourRate,
                BigDecimal kmRate,
                BigDecimal dailyCharge,
                BigDecimal hourCharge,
                BigDecimal kmCharge,
                BigDecimal total) {
            this.hireType = hireType;
            this.startMileageKm = startMileageKm;
            this.returnMileageKm = returnMileageKm;
            this.tripKm = tripKm;
            this.days = days;
            this.extraHours = extraHours;
            this.freeKmPerDay = freeKmPerDay;
            this.includedKm = includedKm;
            this.billableExtraKm = billableExtraKm;
            this.dailyRate = dailyRate;
            this.hourRate = hourRate;
            this.kmRate = kmRate;
            this.dailyCharge = dailyCharge;
            this.hourCharge = hourCharge;
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

        public BigDecimal getExtraHours() {
            return extraHours;
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

        public BigDecimal getHourRate() {
            return hourRate;
        }

        public BigDecimal getKmRate() {
            return kmRate;
        }

        public BigDecimal getDailyCharge() {
            return dailyCharge;
        }

        public BigDecimal getHourCharge() {
            return hourCharge;
        }

        public BigDecimal getKmCharge() {
            return kmCharge;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }
}
