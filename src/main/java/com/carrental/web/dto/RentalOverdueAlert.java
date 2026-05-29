package com.carrental.web.dto;

import java.time.LocalDate;

public class RentalOverdueAlert {

    private final Long rentalId;
    private final String registrationNumber;
    private final String customerName;
    private final LocalDate expectedReturnDate;
    private final long daysOverdue;

    public RentalOverdueAlert(
            Long rentalId,
            String registrationNumber,
            String customerName,
            LocalDate expectedReturnDate,
            long daysOverdue) {
        this.rentalId = rentalId;
        this.registrationNumber = registrationNumber;
        this.customerName = customerName;
        this.expectedReturnDate = expectedReturnDate;
        this.daysOverdue = daysOverdue;
    }

    public Long getRentalId() {
        return rentalId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public long getDaysOverdue() {
        return daysOverdue;
    }

    public String getDaysOverdueLabel() {
        return daysOverdue == 1 ? "1 day overdue" : daysOverdue + " days overdue";
    }
}
