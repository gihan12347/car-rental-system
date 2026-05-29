package com.carrental.service;

import com.carrental.model.Employee;
import com.carrental.model.EmployeePayment;
import com.carrental.repository.EmployeePaymentRepository;
import com.carrental.web.SearchQuery;
import com.carrental.web.dto.EmployeePaymentForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class EmployeePaymentService {

    private final EmployeePaymentRepository employeePaymentRepository;
    private final EmployeeService employeeService;

    public EmployeePaymentService(
            EmployeePaymentRepository employeePaymentRepository,
            EmployeeService employeeService) {
        this.employeePaymentRepository = employeePaymentRepository;
        this.employeeService = employeeService;
    }

    public Page<EmployeePayment> listForEmployee(
            Long employeeId,
            int page,
            EmployeePaymentPeriodFilter filter) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                EmployeePaymentPeriodFilter.PAYMENT_PAGE_SIZE);
        return employeePaymentRepository.findByEmployeeIdAndPaymentDateBetween(
                employeeId,
                filter.getRange().getStart(),
                filter.getRange().getEnd(),
                pageable);
    }

    public BigDecimal totalForEmployeeInPeriod(Long employeeId, EmployeePaymentPeriodFilter filter) {
        return employeePaymentRepository.sumByEmployeeIdAndPaymentDateBetween(
                employeeId,
                filter.getRange().getStart(),
                filter.getRange().getEnd());
    }

    public Page<EmployeePayment> searchAll(
            String query,
            int page,
            EmployeePaymentPeriodFilter filter) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                EmployeePaymentPeriodFilter.PAYMENT_PAGE_SIZE);
        String q = SearchQuery.normalize(query);
        LocalDate start = filter.getRange().getStart();
        LocalDate end = filter.getRange().getEnd();
        if (q.isEmpty()) {
            return employeePaymentRepository.findByPaymentDateBetween(start, end, pageable);
        }
        return employeePaymentRepository.searchByTermAndPaymentDateBetween(q, start, end, pageable);
    }

    public BigDecimal totalInPeriod(String query, EmployeePaymentPeriodFilter filter) {
        String q = SearchQuery.normalize(query);
        LocalDate start = filter.getRange().getStart();
        LocalDate end = filter.getRange().getEnd();
        if (q.isEmpty()) {
            return employeePaymentRepository.sumByPaymentDateBetween(start, end);
        }
        return employeePaymentRepository.sumByTermAndPaymentDateBetween(q, start, end);
    }

    public long countInPeriod(EmployeePaymentPeriodFilter filter) {
        LocalDate start = filter.getRange().getStart();
        LocalDate end = filter.getRange().getEnd();
        return employeePaymentRepository.countByPaymentDateBetween(start, end);
    }

    public EmployeePayment getById(Long id) {
        return employeePaymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
    }

    @Transactional
    public EmployeePayment save(EmployeePaymentForm form) {
        if (form.getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee is required.");
        }
        if (form.getPaymentType() == null) {
            throw new IllegalArgumentException("Payment type is required.");
        }
        if (form.getPaymentValue() == null || form.getPaymentValue().signum() < 0) {
            throw new IllegalArgumentException("Payment amount must be zero or positive.");
        }
        if (form.getPaymentDate() == null) {
            throw new IllegalArgumentException("Payment date is required.");
        }

        Employee employee = employeeService.getById(form.getEmployeeId());
        EmployeePayment payment;
        if (form.getId() != null) {
            payment = getById(form.getId());
        } else {
            payment = new EmployeePayment();
        }

        payment.setEmployee(employee);
        payment.setPaymentType(form.getPaymentType());
        payment.setPaymentValue(form.getPaymentValue());
        payment.setPaymentDate(form.getPaymentDate());
        return employeePaymentRepository.save(payment);
    }

    @Transactional
    public void delete(Long id) {
        EmployeePayment payment = getById(id);
        employeePaymentRepository.delete(payment);
    }

    public EmployeePaymentForm toForm(EmployeePayment payment) {
        EmployeePaymentForm form = new EmployeePaymentForm();
        form.setId(payment.getId());
        form.setEmployeeId(payment.getEmployee().getId());
        form.setPaymentType(payment.getPaymentType());
        form.setPaymentValue(payment.getPaymentValue());
        form.setPaymentDate(payment.getPaymentDate());
        return form;
    }
}
