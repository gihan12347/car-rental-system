package com.carrental.car;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CarCalendarDayDetail {

    private String date;
    private String dateLabel;
    private String status;
    private String statusLabel;
    private List<RentalOnDay> rentals = new ArrayList<RentalOnDay>();
    private List<MaintenanceOnDay> maintenance = new ArrayList<MaintenanceOnDay>();

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public void setDateLabel(String dateLabel) {
        this.dateLabel = dateLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public List<RentalOnDay> getRentals() {
        return rentals;
    }

    public void setRentals(List<RentalOnDay> rentals) {
        this.rentals = rentals;
    }

    public List<MaintenanceOnDay> getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(List<MaintenanceOnDay> maintenance) {
        this.maintenance = maintenance;
    }

    public static class RentalOnDay {
        private Long id;
        private String customerName;
        private String customerContact;
        private String customerIdNumber;
        private String customerAddress;
        private String rentalStatus;
        private String period;
        private String pickupDate;
        private String returnDate;
        private Integer numberOfDays;
        private java.math.BigDecimal totalPrice;
        private String travelLocation;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
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

        public String getCustomerAddress() {
            return customerAddress;
        }

        public void setCustomerAddress(String customerAddress) {
            this.customerAddress = customerAddress;
        }

        public String getRentalStatus() {
            return rentalStatus;
        }

        public void setRentalStatus(String rentalStatus) {
            this.rentalStatus = rentalStatus;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getPickupDate() {
            return pickupDate;
        }

        public void setPickupDate(String pickupDate) {
            this.pickupDate = pickupDate;
        }

        public String getReturnDate() {
            return returnDate;
        }

        public void setReturnDate(String returnDate) {
            this.returnDate = returnDate;
        }

        public Integer getNumberOfDays() {
            return numberOfDays;
        }

        public void setNumberOfDays(Integer numberOfDays) {
            this.numberOfDays = numberOfDays;
        }

        public java.math.BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(java.math.BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }

        public String getTravelLocation() {
            return travelLocation;
        }

        public void setTravelLocation(String travelLocation) {
            this.travelLocation = travelLocation;
        }
    }

    public static class MaintenanceOnDay {
        private Long id;
        private String maintenanceDate;
        private String description;
        private BigDecimal cost;
        private Integer mileageKm;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMaintenanceDate() {
            return maintenanceDate;
        }

        public void setMaintenanceDate(String maintenanceDate) {
            this.maintenanceDate = maintenanceDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public Integer getMileageKm() {
            return mileageKm;
        }

        public void setMileageKm(Integer mileageKm) {
            this.mileageKm = mileageKm;
        }
    }
}
