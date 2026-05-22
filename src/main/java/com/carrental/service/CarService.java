package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.model.CarStatus;
import com.carrental.repository.CarRepository;
import com.carrental.web.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> listAll() {
        return carRepository.findAllByOrderByRegistrationNumberAsc();
    }

    public List<Car> search(String query) {
        String q = SearchQuery.normalize(query);
        if (q.isEmpty()) {
            return listAll();
        }
        return carRepository.searchByTerm(q);
    }

    public List<Car> listAvailable() {
        return carRepository.findByStatusOrderByRegistrationNumberAsc(CarStatus.AVAILABLE);
    }

    public Car getById(Long id) {
        return carRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Car not found: " + id));
    }

    @Transactional
    public Car save(Car car) {
        if (car.getModelName() == null || car.getModelName().trim().isEmpty()) {
            car.setModelName(car.getRegistrationNumber());
        }
        if (car.getVehicleType() == null) {
            car.setVehicleType(com.carrental.model.VehicleType.SEDAN);
        }
        return carRepository.save(car);
    }

    @Transactional
    public void setStatus(Long carId, CarStatus status) {
        Car car = getById(carId);
        car.setStatus(status);
        carRepository.save(car);
    }
}
