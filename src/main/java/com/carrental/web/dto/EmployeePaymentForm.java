package com.carrental.web.dto;

import com.carrental.model.PaymentType;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeePaymentForm {

    private Long id;

    @NotNull(message = "Employee is required.")
    private Long employeeId;

    @NotNull(message = "Payment type is required.")
    private PaymentType paymentType = PaymentType.SALARY;

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = "0", message = "Amount must be zero or positive.")
    private BigDecimal paymentValue;

    @NotNull(message = "Payment date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paymentDate = LocalDate.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getPaymentValue() {
        return paymentValue;
    }

    public void setPaymentValue(BigDecimal paymentValue) {
        this.paymentValue = paymentValue;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
}
