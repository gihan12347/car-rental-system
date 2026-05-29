package com.carrental.repository;

import com.carrental.model.BlacklistedCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlacklistedCustomerRepository extends JpaRepository<BlacklistedCustomer, Long> {

    Optional<BlacklistedCustomer> findByCustomerIdNumber(String customerIdNumber);
}
