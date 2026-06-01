package com.carrental.service;

import com.carrental.model.Car;
import com.carrental.model.CarStatus;
import com.carrental.repository.CarRepository;
import com.carrental.repository.MaintenanceRecordRepository;
import com.carrental.repository.RentalRepository;
import com.carrental.storage.CarImageStorageService;
import com.carrental.web.SearchQuery;
import com.carrental.web.dto.CarDeleteImpact;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final CarImageStorageService carImageStorageService;

    public CarService(
            CarRepository carRepository,
            RentalRepository rentalRepository,
            MaintenanceRecordRepository maintenanceRecordRepository,
            CarImageStorageService carImageStorageService) {
        this.carRepository = carRepository;
        this.rentalRepository = rentalRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.carImageStorageService = carImageStorageService;
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

    public long countRentalsForCar(Long carId) {
        getById(carId);
        return rentalRepository.countByCar_Id(carId);
    }

    public long countMaintenanceForCar(Long carId) {
        getById(carId);
        return maintenanceRecordRepository.countByCar_Id(carId);
    }

    public Map<Long, CarDeleteImpact> mapDeleteImpactByCarId(List<Car> cars) {
        Map<Long, CarDeleteImpact> map = new HashMap<>();
        for (Car car : cars) {
            Long id = car.getId();
            map.put(id, new CarDeleteImpact(
                    rentalRepository.countByCar_Id(id),
                    maintenanceRecordRepository.countByCar_Id(id)));
        }
        return map;
    }

    /**
     * Deletes the vehicle and all rentals and maintenance records linked to it.
     *
     * @return success message with counts removed
     */
    @Transactional
    public String deleteWithMappedRecords(Long carId) {
        Car car = getById(carId);
        long rentals = rentalRepository.countByCar_Id(carId);
        long maintenance = maintenanceRecordRepository.countByCar_Id(carId);
        String registration = car.getRegistrationNumber();

        rentalRepository.deleteByCar_Id(carId);
        maintenanceRecordRepository.deleteByCar_Id(carId);
        carImageStorageService.deleteIfPresent(car.getImagePath());
        carRepository.deleteById(carId);

        return "Vehicle " + registration + " and all linked data removed ("
                + rentals + " rental" + (rentals == 1 ? "" : "s")
                + ", " + maintenance + " maintenance record" + (maintenance == 1 ? "" : "s") + ").";
    }
}
