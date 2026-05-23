package com.carrental.repository;

import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByRentalStatusOrderByHireDateDesc(RentalStatus status);

    List<Rental> findAllByOrderByHireDateDesc();

    @Query("SELECT r FROM Rental r JOIN r.car c WHERE "
            + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.customerAddress) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.customerContact) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.customerIdNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "CONCAT('', r.numberOfDays) LIKE CONCAT('%', :q, '%') "
            + "ORDER BY r.hireDate DESC")
    List<Rental> searchAllByTerm(@Param("q") String q);

    @Query("SELECT r FROM Rental r JOIN r.car c WHERE r.rentalStatus = com.carrental.model.RentalStatus.ACTIVE AND ("
            + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.customerAddress) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.customerContact) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.customerIdNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "CONCAT('', r.numberOfDays) LIKE CONCAT('%', :q, '%')) "
            + "ORDER BY r.hireDate DESC")
    List<Rental> searchActiveByTerm(@Param("q") String q);

    @Query("SELECT r FROM Rental r JOIN FETCH r.car ORDER BY r.hireDate DESC")
    List<Rental> findAllWithCar();

    @Query("SELECT r FROM Rental r JOIN FETCH r.car WHERE r.rentalStatus IN :statuses")
    List<Rental> findBlockingRentalsWithCar(@Param("statuses") Collection<RentalStatus> statuses);

    @Query("SELECT r FROM Rental r WHERE r.car.id = :carId ORDER BY r.pickupDate DESC, r.hireDate DESC")
    List<Rental> findByCarIdOrderByPickupDateDesc(@Param("carId") Long carId);

    @Query("SELECT r FROM Rental r JOIN FETCH r.car WHERE r.id = :id")
    Optional<Rental> findByIdWithCar(@Param("id") Long id);
}
