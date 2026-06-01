package com.carrental.web.dto;

import com.carrental.model.HireType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class RentalForm {

    @NotNull
    private Long carId;

    @NotNull
    private HireType hireType = HireType.PER_DAY;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotBlank
    private String customerName;

    @NotBlank
    private String customerAddress;

    @NotBlank
    private String customerContact;

    @NotBlank(message = "Customer ID number is required.")
    private String customerIdNumber;

    @NotBlank(message = "Travel location is required.")
    private String travelLocation;

    public RentalForm() {
        LocalDate today = LocalDate.now();
        this.startDate = today;
        this.endDate = today.plusDays(1);
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public HireType getHireType() {
        return hireType;
    }

    public void setHireType(HireType hireType) {
        this.hireType = hireType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getCustomerIdNumber() {
        return customerIdNumber;
    }

    public void setCustomerIdNumber(String customerIdNumber) {
        this.customerIdNumber = customerIdNumber;
    }

    public String getTravelLocation() {
        return travelLocation;
    }

    public void setTravelLocation(String travelLocation) {
        this.travelLocation = travelLocation;
    }

    public boolean isValidPeriod() {
        return startDate != null && endDate != null && !endDate.isBefore(startDate);
    }
}
