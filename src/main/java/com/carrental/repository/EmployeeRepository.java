package com.carrental.repository;

import com.carrental.model.Employee;
import com.carrental.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByOrderByNameAsc();

    List<Employee> findByStatusNotOrderByNameAsc(EmployeeStatus status);

    Optional<Employee> findByNicIgnoreCase(String nic);

    boolean existsByNicIgnoreCaseAndIdNot(String nic, Long id);

    boolean existsByNicIgnoreCase(String nic);

    @Query("SELECT e FROM Employee e WHERE "
            + "LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(e.nic) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT('', e.status)) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "ORDER BY e.name ASC")
    List<Employee> searchByTerm(@Param("q") String q);
}
