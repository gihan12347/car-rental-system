package com.carrental.repository;

import com.carrental.model.EmployeePayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EmployeePaymentRepository extends JpaRepository<EmployeePayment, Long> {

    @EntityGraph(attributePaths = "employee")
    Page<EmployeePayment> findByPaymentDateBetween(
            LocalDate start,
            LocalDate end,
            Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    @Query("SELECT p FROM EmployeePayment p WHERE p.paymentDate BETWEEN :start AND :end AND ("
            + "LOWER(p.employee.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(p.employee.nic) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT('', p.paymentType)) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "ORDER BY p.paymentDate DESC, p.id DESC")
    Page<EmployeePayment> searchByTermAndPaymentDateBetween(
            @Param("q") String q,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    @EntityGraph(attributePaths = "employee")
    Page<EmployeePayment> findByEmployeeIdAndPaymentDateBetween(
            Long employeeId,
            LocalDate start,
            LocalDate end,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.paymentValue), 0) FROM EmployeePayment p "
            + "WHERE p.employee.id = :employeeId AND p.paymentDate BETWEEN :start AND :end")
    BigDecimal sumByEmployeeIdAndPaymentDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    long countByPaymentDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(p.paymentValue), 0) FROM EmployeePayment p "
            + "WHERE p.paymentDate BETWEEN :start AND :end")
    BigDecimal sumByPaymentDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(p.paymentValue), 0) FROM EmployeePayment p "
            + "WHERE p.paymentDate BETWEEN :start AND :end AND ("
            + "LOWER(p.employee.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(p.employee.nic) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT('', p.paymentType)) LIKE LOWER(CONCAT('%', :q, '%')))")
    BigDecimal sumByTermAndPaymentDateBetween(
            @Param("q") String q,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    List<EmployeePayment> findByEmployeeIdOrderByPaymentDateDesc(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}
