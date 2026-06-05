package com.carrental.web.dto;

import com.carrental.model.HireType;

import java.math.BigDecimal;

public class AvailableCarOption {

    private final Long id;
    private final String registrationNumber;
    private final String modelName;
    private final Integer passengerCount;
    private final BigDecimal rentalPricePerDay;
    private final BigDecimal rentalPricePerWeek;
    private final BigDecimal rentalPricePerMonth;
    private final BigDecimal extraPricePerHour;

    public AvailableCarOption(
            Long id,
            String registrationNumber,
            String modelName,
            Integer passengerCount,
            BigDecimal rentalPricePerDay,
            BigDecimal rentalPricePerWeek,
            BigDecimal rentalPricePerMonth,
            BigDecimal extraPricePerHour) {
        this.id = id;
        this.registrationNumber = registrationNumber;
        this.modelName = modelName;
        this.passengerCount = passengerCount;
        this.rentalPricePerDay = rentalPricePerDay;
        this.rentalPricePerWeek = rentalPricePerWeek;
        this.rentalPricePerMonth = rentalPricePerMonth;
        this.extraPricePerHour = extraPricePerHour;
    }

    public Long getId() {
        return id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getModelName() {
        return modelName;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public BigDecimal getRentalPricePerDay() {
        return rentalPricePerDay;
    }

    public BigDecimal getRentalPricePerWeek() {
        return rentalPricePerWeek;
    }

    public BigDecimal getRentalPricePerMonth() {
        return rentalPricePerMonth;
    }

    public BigDecimal getExtraPricePerHour() {
        return extraPricePerHour;
    }

    public String getLabel() {
        return buildLabel(HireType.PER_DAY);
    }

    public String buildLabel(HireType hireType) {
        String name = modelName != null && !modelName.trim().isEmpty()
                ? modelName
                : registrationNumber;
        BigDecimal rate = rateForType(hireType);
        return name + " · " + registrationNumber + " · " + passengerCount + " seats · "
                + rate + " / day";
    }

    private BigDecimal rateForType(HireType hireType) {
        HireType type = hireType != null ? hireType : HireType.PER_DAY;
        switch (type) {
            case PER_WEEK:
                return rentalPricePerWeek;
            case PER_MONTH:
                return rentalPricePerMonth;
            case PER_DAY:
            default:
                return rentalPricePerDay;
        }
    }
}
