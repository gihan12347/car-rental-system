package com.carrental.service;

import com.carrental.model.Car;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class RentalPricingHelper {

    private RentalPricingHelper() {
    }

    public static PriceBreakdown calculate(
            Car car,
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

        BigDecimal dailyRate = car.getRentalPricePerDay() != null ? car.getRentalPricePerDay() : BigDecimal.ZERO;
        BigDecimal kmRate = car.getExtraPricePerKm() != null ? car.getExtraPricePerKm() : BigDecimal.ZERO;

        BigDecimal dailyCharge = dailyRate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal kmCharge = kmRate.multiply(BigDecimal.valueOf(billableExtraKm)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = dailyCharge.add(kmCharge).setScale(2, RoundingMode.HALF_UP);

        return new PriceBreakdown(
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

    public static class PriceBreakdown {
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
