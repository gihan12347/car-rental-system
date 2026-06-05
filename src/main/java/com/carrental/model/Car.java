package com.carrental.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(length = 120)
    private String modelName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VehicleType vehicleType = VehicleType.SEDAN;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer mileageKm;

    @Column
    private Integer nextServiceKm;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer passengerCount;

    @NotNull
    @Column(nullable = false)
    private Boolean withDriver;

    @NotNull
    @Min(0)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal rentalPricePerDay;

    @NotNull
    @Min(0)
    /** Per-day rate when hire type is weekly. */
    @Column(name = "rental_price_per_week", nullable = false, precision = 12, scale = 2)
    private BigDecimal rentalPricePerWeek;

    /** Per-day rate when hire type is monthly. */
    @NotNull
    @Min(0)
    @Column(name = "rental_price_per_month", nullable = false, precision = 12, scale = 2)
    private BigDecimal rentalPricePerMonth;

    @NotNull
    @Min(0)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal extraPricePerKm;

    @NotNull
    @Min(0)
    @Column(name = "extra_price_per_hour", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraPricePerHour = BigDecimal.ZERO;

    @NotNull
    @Min(0)
    @Column(name = "free_km_per_day", nullable = false)
    private Integer freeKmPerDay = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CarStatus status = CarStatus.AVAILABLE;

    /** Web path e.g. /uploads/cars/uuid.jpg — optional */
    @Column(length = 512)
    private String imagePath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getMileageKm() {
        return mileageKm;
    }

    public void setMileageKm(Integer mileageKm) {
        this.mileageKm = mileageKm;
    }

    public Integer getNextServiceKm() {
        return nextServiceKm;
    }

    public void setNextServiceKm(Integer nextServiceKm) {
        this.nextServiceKm = nextServiceKm;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public Boolean getWithDriver() {
        return withDriver;
    }

    public void setWithDriver(Boolean withDriver) {
        this.withDriver = withDriver;
    }

    public BigDecimal getRentalPricePerDay() {
        return rentalPricePerDay;
    }

    public void setRentalPricePerDay(BigDecimal rentalPricePerDay) {
        this.rentalPricePerDay = rentalPricePerDay;
    }

    public BigDecimal getRentalPricePerWeek() {
        return rentalPricePerWeek;
    }

    public void setRentalPricePerWeek(BigDecimal rentalPricePerWeek) {
        this.rentalPricePerWeek = rentalPricePerWeek;
    }

    public BigDecimal getRentalPricePerMonth() {
        return rentalPricePerMonth;
    }

    public void setRentalPricePerMonth(BigDecimal rentalPricePerMonth) {
        this.rentalPricePerMonth = rentalPricePerMonth;
    }

    public BigDecimal getExtraPricePerKm() {
        return extraPricePerKm;
    }

    public void setExtraPricePerKm(BigDecimal extraPricePerKm) {
        this.extraPricePerKm = extraPricePerKm;
    }

    public BigDecimal getExtraPricePerHour() {
        return extraPricePerHour;
    }

    public void setExtraPricePerHour(BigDecimal extraPricePerHour) {
        this.extraPricePerHour = extraPricePerHour;
    }

    public Integer getFreeKmPerDay() {
        return freeKmPerDay;
    }

    public void setFreeKmPerDay(Integer freeKmPerDay) {
        this.freeKmPerDay = freeKmPerDay;
    }

    public CarStatus getStatus() {
        return status;
    }

    public void setStatus(CarStatus status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
