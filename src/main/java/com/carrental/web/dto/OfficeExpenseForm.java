package com.carrental.web.dto;

import com.carrental.model.OfficeExpenseCategory;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OfficeExpenseForm {

    private Long id;

    @NotNull(message = "Category is required.")
    private OfficeExpenseCategory category = OfficeExpenseCategory.OTHER;

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0", message = "Amount must be zero or positive.")
    private BigDecimal amount;

    @NotNull(message = "Expense date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseDate = LocalDate.now();

    @Size(max = 64)
    private String referenceNumber;

    @Size(max = 1000)
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OfficeExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(OfficeExpenseCategory category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
