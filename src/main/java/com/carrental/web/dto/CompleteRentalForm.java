package com.carrental.web.dto;

import com.carrental.model.Rental;
import com.carrental.service.RentalPeriodHelper;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CompleteRentalForm {

    private Long rentalId;

    @NotNull(message = "Return date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    @NotNull(message = "Return time is required.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime returnTime;

    @NotNull(message = "Return mileage is required.")
    @Min(value = 0, message = "Return mileage must be zero or positive.")
    private Integer returnMileageKm;

    private Boolean documentReturned = Boolean.FALSE;

    private Boolean blacklistCustomer = Boolean.FALSE;

    private String blacklistReason;

    @Min(value = 0, message = "Discount must be zero or positive.")
    private BigDecimal discount = BigDecimal.ZERO;

    @Size(max = 1000, message = "Comment must be at most 1000 characters.")
    private String completionComment;

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

    public LocalTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalTime returnTime) {
        this.returnTime = returnTime;
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

    public Boolean getBlacklistCustomer() {
        return blacklistCustomer;
    }

    public void setBlacklistCustomer(Boolean blacklistCustomer) {
        this.blacklistCustomer = blacklistCustomer;
    }

    public String getBlacklistReason() {
        return blacklistReason;
    }

    public void setBlacklistReason(String blacklistReason) {
        this.blacklistReason = blacklistReason;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getCompletionComment() {
        return completionComment;
    }

    public void setCompletionComment(String completionComment) {
        this.completionComment = completionComment;
    }

    /** Defaults return date/time to the rental's booked (planned) return in the database. */
    public static CompleteRentalForm forActiveRental(Rental rental) {
        CompleteRentalForm form = new CompleteRentalForm();
        if (rental != null) {
            form.setRentalId(rental.getId());
            LocalDateTime plannedReturn = RentalPeriodHelper.plannedReturnDateTime(rental);
            form.setReturnDate(plannedReturn.toLocalDate());
            form.setReturnTime(plannedReturn.toLocalTime().withSecond(0).withNano(0));
        }
        return form;
    }
}
