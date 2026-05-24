package com.carrental.web.dto;

public class ServiceOverdueAlert {

    private final Long carId;
    private final String registrationNumber;
    private final String modelName;
    private final Integer mileageKm;
    private final Integer nextServiceKm;
    private final int kmOverdue;

    public ServiceOverdueAlert(
            Long carId,
            String registrationNumber,
            String modelName,
            Integer mileageKm,
            Integer nextServiceKm,
            int kmOverdue) {
        this.carId = carId;
        this.registrationNumber = registrationNumber;
        this.modelName = modelName;
        this.mileageKm = mileageKm;
        this.nextServiceKm = nextServiceKm;
        this.kmOverdue = kmOverdue;
    }

    public Long getCarId() {
        return carId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getModelName() {
        return modelName;
    }

    public Integer getMileageKm() {
        return mileageKm;
    }

    public Integer getNextServiceKm() {
        return nextServiceKm;
    }

    public int getKmOverdue() {
        return kmOverdue;
    }

    public String getDisplayName() {
        if (modelName != null && !modelName.trim().isEmpty()) {
            return modelName.trim();
        }
        return registrationNumber;
    }
}
