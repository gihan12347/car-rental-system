package com.carrental.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer numberOfDays;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hire_type", nullable = false, length = 32)
    private HireType hireType = HireType.PER_DAY;

    @NotBlank
    @Column(nullable = false)
    private String customerName;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String customerAddress;

    @NotBlank
    @Column(nullable = false)
    private String customerContact;

    @NotBlank
    @Column(name = "customer_id_number", nullable = false, length = 64)
    private String customerIdNumber;

    @NotBlank
    @Column(name = "travel_location", nullable = false, length = 500)
    private String travelLocation;

    @NotNull
    @Column(nullable = false)
    private LocalDate hireDate = LocalDate.now();

    @NotNull
    @Column(nullable = false)
    private LocalDate pickupDate = LocalDate.now();

    @Column
    private LocalDate returnDate;

    @Column
    private LocalDate completedDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RentalStatus rentalStatus = RentalStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean customerComplaint = Boolean.FALSE;

    @Min(0)
    @Column(precision = 12, scale = 2)
    private BigDecimal extraKm;

    @Min(0)
    @Column(precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Min(0)
    @Column(name = "completion_discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal completionDiscount = BigDecimal.ZERO;

    @Column(name = "completion_comment", length = 1000)
    private String completionComment;

    @Column(nullable = false)
    private Boolean documentReturned = Boolean.FALSE;

    @Column(name = "employee_hire", nullable = false)
    private Boolean employeeHire = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public HireType getHireType() {
        return hireType;
    }

    public void setHireType(HireType hireType) {
        this.hireType = hireType;
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

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public Boolean getCustomerComplaint() {
        return customerComplaint;
    }

    public void setCustomerComplaint(Boolean customerComplaint) {
        this.customerComplaint = customerComplaint;
    }

    public RentalStatus getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(RentalStatus rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public BigDecimal getExtraKm() {
        return extraKm;
    }

    public void setExtraKm(BigDecimal extraKm) {
        this.extraKm = extraKm;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getCompletionDiscount() {
        return completionDiscount;
    }

    public void setCompletionDiscount(BigDecimal completionDiscount) {
        this.completionDiscount = completionDiscount;
    }

    public String getCompletionComment() {
        return completionComment;
    }

    public void setCompletionComment(String completionComment) {
        this.completionComment = completionComment;
    }

    public Boolean getDocumentReturned() {
        return documentReturned;
    }

    public void setDocumentReturned(Boolean documentReturned) {
        this.documentReturned = documentReturned;
    }

    public Boolean getEmployeeHire() {
        return employeeHire;
    }

    public void setEmployeeHire(Boolean employeeHire) {
        this.employeeHire = employeeHire;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
