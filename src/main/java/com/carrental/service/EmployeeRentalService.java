package com.carrental.service;

import com.carrental.model.Rental;
import com.carrental.repository.RentalRepository;
import com.carrental.service.EmployeePaymentPeriodFilter;
import com.carrental.util.NicNormalizer;
import com.carrental.web.SearchQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmployeeRentalService {

    public static final int EMPLOYEE_RENTALS_PAGE_SIZE = 10;

    private final RentalRepository rentalRepository;

    public EmployeeRentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public Page<Rental> listAllEmployeeHires(String query, int page, EmployeePaymentPeriodFilter filter) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                EMPLOYEE_RENTALS_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "hireDate"));
        String q = SearchQuery.normalize(query);
        LocalDate start = filter.getRange().getStart();
        LocalDate end = filter.getRange().getEnd();
        if (q.isEmpty()) {
            return rentalRepository.findEmployeeHiresByHireDateBetween(start, end, pageable);
        }
        return rentalRepository.searchEmployeeHiresByHireDateBetween(
                q, SearchQuery.normalizePlate(query), start, end, pageable);
    }

    public long countAllEmployeeHiresInPeriod(EmployeePaymentPeriodFilter filter) {
        return rentalRepository.countEmployeeHiresByHireDateBetween(
                filter.getRange().getStart(),
                filter.getRange().getEnd());
    }

    public Page<Rental> listForEmployee(Long employeeId, String query, int page, EmployeePaymentPeriodFilter filter) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                EMPLOYEE_RENTALS_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "hireDate"));
        String q = SearchQuery.normalize(query);
        LocalDate start = filter.getRange().getStart();
        LocalDate end = filter.getRange().getEnd();
        if (q.isEmpty()) {
            return rentalRepository.findByEmployeeIdAndHireDateBetween(employeeId, start, end, pageable);
        }
        return rentalRepository.searchByEmployeeIdAndHireDateBetween(
                employeeId, q, SearchQuery.normalizePlate(query), start, end, pageable);
    }

    public long countForEmployeeInPeriod(Long employeeId, EmployeePaymentPeriodFilter filter) {
        return rentalRepository.countByEmployee_IdAndHireDateBetween(
                employeeId,
                filter.getRange().getStart(),
                filter.getRange().getEnd());
    }

    public long countAllEmployeeHires() {
        return rentalRepository.countByEmployeeHireTrue();
    }

    public long countForEmployee(Long employeeId) {
        return rentalRepository.countByEmployee_Id(employeeId);
    }

    public static boolean isEmployeeNic(String customerIdNumber, String employeeNic) {
        return !NicNormalizer.normalize(customerIdNumber).isEmpty()
                && NicNormalizer.normalize(customerIdNumber).equals(NicNormalizer.normalize(employeeNic));
    }
}
