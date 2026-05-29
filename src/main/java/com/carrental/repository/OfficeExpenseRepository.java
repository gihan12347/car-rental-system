package com.carrental.repository;

import com.carrental.model.OfficeExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OfficeExpenseRepository extends JpaRepository<OfficeExpense, Long> {

    Page<OfficeExpense> findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
            LocalDate start,
            LocalDate end,
            Pageable pageable);

    @Query("SELECT e FROM OfficeExpense e WHERE e.expenseDate BETWEEN :start AND :end AND ("
            + "LOWER(e.referenceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(e.notes) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT('', e.category)) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "ORDER BY e.expenseDate DESC, e.id DESC")
    Page<OfficeExpense> searchByTermAndExpenseDateBetween(
            @Param("q") String q,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    long countByExpenseDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM OfficeExpense e "
            + "WHERE e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumByExpenseDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM OfficeExpense e "
            + "WHERE e.expenseDate BETWEEN :start AND :end AND ("
            + "LOWER(e.referenceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(e.notes) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT('', e.category)) LIKE LOWER(CONCAT('%', :q, '%')))")
    BigDecimal sumByTermAndExpenseDateBetween(
            @Param("q") String q,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
