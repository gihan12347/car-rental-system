package com.carrental.repository;

import com.carrental.model.Employee;
import com.carrental.model.EmployeeStatus;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Cacheable(value = "employeesAllSorted")
    List<Employee> findAllByOrderByNameAsc();

    @Cacheable(value = "employeesAllSorted")
    List<Employee> findByStatusNotOrderByNameAsc(EmployeeStatus status);

    Optional<Employee> findByNicIgnoreCase(String nic);

    boolean existsByNicIgnoreCaseAndIdNot(String nic, Long id);

    @Cacheable(value = "employeesByNic", key = "#nic.toLowerCase()")
    boolean existsByNicIgnoreCase(String nic);

    @Query("SELECT e FROM Employee e WHERE "
            + "LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(e.nic) LIKE LOWER(CONCAT('%', :q, '%')) OR "
            + "LOWER(CONCAT('', e.status)) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "ORDER BY e.name ASC")
    @Cacheable(value = "employeesSearch", key = "#q.toLowerCase()")
    List<Employee> searchByTerm(@Param("q") String q);
}
