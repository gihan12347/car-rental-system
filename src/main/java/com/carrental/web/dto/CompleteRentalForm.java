package com.carrental.web.dto;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class CompleteRentalForm {

    private Long rentalId;

    @NotNull(message = "Return date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    @NotNull(message = "Return mileage is required.")
    @Min(value = 0, message = "Return mileage must be zero or positive.")
    private Integer returnMileageKm;

    private Boolean documentReturned = Boolean.FALSE;

    public Long getRentalId() {
        return rentalId;
    }

    public void setRentalId(Long rentalId) {
        this.rentalId = rentalId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getReturnMileageKm() {
        return returnMileageKm;
    }

    public void setReturnMileageKm(Integer returnMileageKm) {
        this.returnMileageKm = returnMileageKm;
    }

    public Boolean getDocumentReturned() {
        return documentReturned;
    }

    public void setDocumentReturned(Boolean documentReturned) {
        this.documentReturned = documentReturned;
    }
}
