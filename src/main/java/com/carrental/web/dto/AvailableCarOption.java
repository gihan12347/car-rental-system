package com.carrental.web.dto;

import java.math.BigDecimal;

public class AvailableCarOption {

    private Long id;
    private String registrationNumber;
    private String modelName;
    private Integer passengerCount;
    private BigDecimal rentalPricePerDay;

    public AvailableCarOption(Long id, String registrationNumber, String modelName,
            Integer passengerCount, BigDecimal rentalPricePerDay) {
        this.id = id;
        this.registrationNumber = registrationNumber;
        this.modelName = modelName;
        this.passengerCount = passengerCount;
        this.rentalPricePerDay = rentalPricePerDay;
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

    public String getLabel() {
        String name = modelName != null && !modelName.trim().isEmpty()
                ? modelName
                : registrationNumber;
        return name + " · " + registrationNumber + " · " + passengerCount + " seats · "
                + rentalPricePerDay + " / day";
    }
}
