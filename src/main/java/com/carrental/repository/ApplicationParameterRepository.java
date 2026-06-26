package com.carrental.repository;

import com.carrental.model.ApplicationParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationParameterRepository extends JpaRepository<ApplicationParameter, String> {

    ApplicationParameter findByCode(String code);
}
