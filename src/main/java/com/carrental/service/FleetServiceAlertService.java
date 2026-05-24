package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.repository.CarRepository;
import com.carrental.web.dto.ServiceOverdueAlert;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FleetServiceAlertService {

    public static final int SERVICE_WARNING_KM = 500;

    private final CarRepository carRepository;

    public FleetServiceAlertService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<ServiceOverdueAlert> findServiceOverdue() {
        List<ServiceOverdueAlert> alerts = new ArrayList<ServiceOverdueAlert>();
        for (Car car : carRepository.findServiceOverdue()) {
            alerts.add(toAlert(car));
        }
        return alerts;
    }

    public int countServiceOverdue() {
        return carRepository.findServiceOverdue().size();
    }

    public static boolean isServiceOverdue(Car car) {
        if (car == null || car.getNextServiceKm() == null || car.getMileageKm() == null) {
            return false;
        }
        return car.getMileageKm() >= car.getNextServiceKm();
    }

    public static boolean isServiceDueSoon(Car car) {
        if (car == null || car.getNextServiceKm() == null || car.getMileageKm() == null) {
            return false;
        }
        if (isServiceOverdue(car)) {
            return false;
        }
        return car.getMileageKm() >= car.getNextServiceKm() - SERVICE_WARNING_KM;
    }

    private static ServiceOverdueAlert toAlert(Car car) {
        int overdue = car.getMileageKm() - car.getNextServiceKm();
        return new ServiceOverdueAlert(
                car.getId(),
                car.getRegistrationNumber(),
                car.getModelName(),
                car.getMileageKm(),
                car.getNextServiceKm(),
                Math.max(0, overdue));
    }
}
