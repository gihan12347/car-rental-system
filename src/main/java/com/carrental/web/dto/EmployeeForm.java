package com.carrental.web.dto;

import com.carrental.model.EmployeeStatus;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class EmployeeForm {

    private Long id;

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "NIC is required.")
    private String nic;

    @NotNull(message = "Job start date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate jobStartDate = LocalDate.now();

    @NotNull(message = "Status is required.")
    private EmployeeStatus status = EmployeeStatus.PERMANENT;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public LocalDate getJobStartDate() {
        return jobStartDate;
    }

    public void setJobStartDate(LocalDate jobStartDate) {
        this.jobStartDate = jobStartDate;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }
}
