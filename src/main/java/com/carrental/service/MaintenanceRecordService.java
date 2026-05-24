package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.model.MaintenanceRecord;
import com.carrental.model.MaintenanceType;
import com.carrental.repository.MaintenanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceRecordService {

    /** Default km added to odometer at service when next service is not specified. */
    public static final int DEFAULT_SERVICE_INTERVAL_KM = 5000;

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final CarService carService;

    public MaintenanceRecordService(
            MaintenanceRecordRepository maintenanceRecordRepository,
            CarService carService) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.carService = carService;
    }

    public List<MaintenanceRecord> listForCar(Long carId) {
        return maintenanceRecordRepository.findByCarIdOrderByMaintenanceDateDesc(carId);
    }

    public List<MaintenanceRecord> listForCarBetween(Long carId, LocalDate start, LocalDate end) {
        return maintenanceRecordRepository.findByCarIdAndMaintenanceDateBetweenOrderByMaintenanceDateAsc(
                carId, start, end);
    }

    @Transactional
    public MaintenanceRecord create(
            Long carId,
            LocalDate maintenanceDate,
            String description,
            BigDecimal cost,
            MaintenanceType maintenanceType,
            Integer nextServiceKm) {
        if (maintenanceDate == null) {
            throw new IllegalArgumentException("Maintenance date is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required.");
        }
        if (cost == null || cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost must be zero or positive.");
        }
        Car car = carService.getById(carId);
        MaintenanceRecord record = new MaintenanceRecord();
        record.setCar(car);
        record.setMaintenanceDate(maintenanceDate);
        record.setDescription(description.trim());
        record.setCost(cost);
        record.setMaintenanceType(maintenanceType != null ? maintenanceType : MaintenanceType.OTHER);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);

        if (record.getMaintenanceType() == MaintenanceType.SERVICE) {
            Integer resolvedNextService = resolveNextServiceKm(car, nextServiceKm);
            if (resolvedNextService != null) {
                car.setNextServiceKm(resolvedNextService);
                carService.save(car);
            }
        }
        return saved;
    }

    private static Integer resolveNextServiceKm(Car car, Integer nextServiceKm) {
        if (nextServiceKm != null && nextServiceKm > 0) {
            return nextServiceKm;
        }
        int baseKm = car.getMileageKm() != null ? car.getMileageKm() : 0;
        if (baseKm <= 0) {
            return null;
        }
        return baseKm + DEFAULT_SERVICE_INTERVAL_KM;
    }
}
