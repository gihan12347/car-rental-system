package com.carrental.repository;

import com.carrental.model.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.car.id = :carId ORDER BY m.maintenanceDate DESC, m.id DESC")
    List<MaintenanceRecord> findByCarIdOrderByMaintenanceDateDesc(@Param("carId") Long carId);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.car.id = :carId ORDER BY m.maintenanceDate DESC, m.id DESC")
    Page<MaintenanceRecord> findByCarIdOrderByMaintenanceDateDesc(@Param("carId") Long carId, Pageable pageable);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.car.id = :carId "
            + "AND m.maintenanceDate BETWEEN :start AND :end ORDER BY m.maintenanceDate ASC")
    List<MaintenanceRecord> findByCarIdAndMaintenanceDateBetweenOrderByMaintenanceDateAsc(
            @Param("carId") Long carId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
