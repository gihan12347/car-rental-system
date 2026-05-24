package com.carrental.repository;

import com.carrental.model.Car;
import com.carrental.model.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByStatusOrderByRegistrationNumberAsc(CarStatus status);

    List<Car> findAllByOrderByRegistrationNumberAsc();

    @Query("SELECT c FROM Car c WHERE c.nextServiceKm IS NOT NULL AND c.mileageKm >= c.nextServiceKm "
            + "ORDER BY c.registrationNumber ASC")
    List<Car> findServiceOverdue();

    @Query("SELECT c FROM Car c WHERE "
            + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT('', c.status)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "CONCAT('', c.mileageKm) LIKE CONCAT('%', :q, '%') OR "
            + "CONCAT('', c.passengerCount) LIKE CONCAT('%', :q, '%') "
            + "ORDER BY c.registrationNumber ASC")
    List<Car> searchByTerm(@Param("q") String q);
}
