package com.carrental.repository;

import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByRentalStatusOrderByHireDateDesc(RentalStatus status);

    Page<Rental> findAllByRentalStatusNotOrderByHireDateDesc(RentalStatus rentalStatus, Pageable pageable);

    @Query(
            value = "SELECT r FROM Rental r JOIN r.car c WHERE "
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerAddress) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerContact) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerIdNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "CONCAT('', r.numberOfDays) LIKE CONCAT('%', :q, '%')",
            countQuery = "SELECT count(r) FROM Rental r JOIN r.car c WHERE "
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerAddress) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerContact) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerIdNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "CONCAT('', r.numberOfDays) LIKE CONCAT('%', :q, '%')")
    Page<Rental> searchAllByTerm(@Param("q") String q, Pageable pageable);

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

    @Query("SELECT r FROM Rental r WHERE r.car.id = :carId ORDER BY r.pickupDate DESC, r.hireDate DESC")
    Page<Rental> findByCarIdOrderByPickupDateDesc(@Param("carId") Long carId, Pageable pageable);

    @Query("SELECT r FROM Rental r JOIN FETCH r.car WHERE r.id = :id")
    Optional<Rental> findByIdWithCar(@Param("id") Long id);

    @Query(
            value = "SELECT r FROM Rental r JOIN FETCH r.car c LEFT JOIN FETCH r.employee e "
                    + "WHERE r.employeeHire = true ORDER BY r.hireDate DESC",
            countQuery = "SELECT count(r) FROM Rental r WHERE r.employeeHire = true")
    Page<Rental> findEmployeeHires(Pageable pageable);

    @Query(
            value = "SELECT r FROM Rental r JOIN FETCH r.car c LEFT JOIN FETCH r.employee e "
                    + "WHERE r.employeeHire = true AND ("
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerIdNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%'))) "
                    + "ORDER BY r.hireDate DESC",
            countQuery = "SELECT count(r) FROM Rental r JOIN r.car c LEFT JOIN r.employee e "
                    + "WHERE r.employeeHire = true AND ("
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerIdNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Rental> searchEmployeeHires(@Param("q") String q, Pageable pageable);

    @Query(
            value = "SELECT r FROM Rental r JOIN FETCH r.car c LEFT JOIN FETCH r.employee e "
                    + "WHERE r.employeeHire = true AND r.hireDate BETWEEN :start AND :end "
                    + "ORDER BY r.hireDate DESC",
            countQuery = "SELECT count(r) FROM Rental r WHERE r.employeeHire = true "
                    + "AND r.hireDate BETWEEN :start AND :end")
    Page<Rental> findEmployeeHiresByHireDateBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    @Query(
            value = "SELECT r FROM Rental r JOIN FETCH r.car c LEFT JOIN FETCH r.employee e "
                    + "WHERE r.employeeHire = true AND r.hireDate BETWEEN :start AND :end AND ("
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "(:qPlate <> '' AND LOWER(REPLACE(REPLACE(c.registrationNumber, '-', ''), ' ', '')) LIKE CONCAT('%', :qPlate, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerIdNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(e.nic) LIKE LOWER(CONCAT('%', :q, '%'))) "
                    + "ORDER BY r.hireDate DESC",
            countQuery = "SELECT count(r) FROM Rental r JOIN r.car c LEFT JOIN r.employee e "
                    + "WHERE r.employeeHire = true AND r.hireDate BETWEEN :start AND :end AND ("
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "(:qPlate <> '' AND LOWER(REPLACE(REPLACE(c.registrationNumber, '-', ''), ' ', '')) LIKE CONCAT('%', :qPlate, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerIdNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(e.nic) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Rental> searchEmployeeHiresByHireDateBetween(
            @Param("q") String q,
            @Param("qPlate") String qPlate,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    @Query("SELECT count(r) FROM Rental r WHERE r.employeeHire = true "
            + "AND r.hireDate BETWEEN :start AND :end")
    long countEmployeeHiresByHireDateBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query(
            value = "SELECT r FROM Rental r JOIN FETCH r.car c LEFT JOIN FETCH r.employee e "
                    + "WHERE r.employee.id = :employeeId ORDER BY r.hireDate DESC",
            countQuery = "SELECT count(r) FROM Rental r WHERE r.employee.id = :employeeId")
    Page<Rental> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    @Query(
            value = "SELECT r FROM Rental r JOIN FETCH r.car c LEFT JOIN FETCH r.employee e "
                    + "WHERE r.employee.id = :employeeId AND ("
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%'))) "
                    + "ORDER BY r.hireDate DESC",
            countQuery = "SELECT count(r) FROM Rental r JOIN r.car c "
                    + "WHERE r.employee.id = :employeeId AND ("
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Rental> searchByEmployeeId(@Param("employeeId") Long employeeId, @Param("q") String q, Pageable pageable);

    @Query(
            value = "SELECT r FROM Rental r JOIN FETCH r.car c LEFT JOIN FETCH r.employee e "
                    + "WHERE r.employee.id = :employeeId AND r.hireDate BETWEEN :start AND :end "
                    + "ORDER BY r.hireDate DESC",
            countQuery = "SELECT count(r) FROM Rental r WHERE r.employee.id = :employeeId "
                    + "AND r.hireDate BETWEEN :start AND :end")
    Page<Rental> findByEmployeeIdAndHireDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    @Query(
            value = "SELECT r FROM Rental r JOIN FETCH r.car c LEFT JOIN FETCH r.employee e "
                    + "WHERE r.employee.id = :employeeId AND r.hireDate BETWEEN :start AND :end AND ("
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "(:qPlate <> '' AND LOWER(REPLACE(REPLACE(c.registrationNumber, '-', ''), ' ', '')) LIKE CONCAT('%', :qPlate, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%'))) "
                    + "ORDER BY r.hireDate DESC",
            countQuery = "SELECT count(r) FROM Rental r JOIN r.car c "
                    + "WHERE r.employee.id = :employeeId AND r.hireDate BETWEEN :start AND :end AND ("
                    + "LOWER(c.registrationNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "(:qPlate <> '' AND LOWER(REPLACE(REPLACE(c.registrationNumber, '-', ''), ' ', '')) LIKE CONCAT('%', :qPlate, '%')) OR "
                    + "LOWER(r.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(r.travelLocation) LIKE LOWER(CONCAT('%', :q, '%')) OR "
                    + "LOWER(CONCAT('', r.rentalStatus)) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Rental> searchByEmployeeIdAndHireDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("q") String q,
            @Param("qPlate") String qPlate,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    @Query("SELECT count(r) FROM Rental r WHERE r.employee.id = :employeeId "
            + "AND r.hireDate BETWEEN :start AND :end")
    long countByEmployee_IdAndHireDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    long countByEmployeeHireTrue();

    long countByEmployee_Id(Long employeeId);

    long countByCar_Id(Long carId);

    void deleteByCar_Id(Long carId);
}
