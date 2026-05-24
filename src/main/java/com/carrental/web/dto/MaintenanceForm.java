package com.carrental.web.dto;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import com.carrental.model.MaintenanceType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MaintenanceForm {

    @NotNull(message = "Date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maintenanceDate = LocalDate.now();

    @NotBlank(message = "Description is required.")
    private String description;

    @NotNull(message = "Cost is required.")
    @DecimalMin(value = "0", message = "Cost must be zero or positive.")
    private BigDecimal cost;

    @NotNull
    private MaintenanceType maintenanceType = MaintenanceType.OTHER;

    @Min(0)
    private Integer nextServiceKm;

    public LocalDate getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(LocalDate maintenanceDate) {
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

    public MaintenanceType getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(MaintenanceType maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public Integer getNextServiceKm() {
        return nextServiceKm;
    }

    public void setNextServiceKm(Integer nextServiceKm) {
        this.nextServiceKm = nextServiceKm;
    }
}
