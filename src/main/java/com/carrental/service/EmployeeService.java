package com.carrental.service;

import com.carrental.model.Employee;
import com.carrental.model.EmployeeStatus;
import com.carrental.repository.EmployeePaymentRepository;
import com.carrental.repository.EmployeeRepository;
import com.carrental.util.NicNormalizer;
import com.carrental.web.SearchQuery;
import com.carrental.web.dto.EmployeeForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeePaymentRepository employeePaymentRepository;
    private final CacheService cacheService;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeePaymentRepository employeePaymentRepository, CacheService cacheService) {
        this.employeeRepository = employeeRepository;
        this.employeePaymentRepository = employeePaymentRepository;
        this.cacheService = cacheService;
    }

    public List<Employee> listAll() {
        return employeeRepository.findAllByOrderByNameAsc();
    }

    public List<Employee> listForPaymentSelect() {
        return listForPaymentSelect(null);
    }

    public List<Employee> listForPaymentSelect(Long includeEmployeeId) {
        List<Employee> employees = new java.util.ArrayList<Employee>(
                employeeRepository.findByStatusNotOrderByNameAsc(EmployeeStatus.RESIGNED));
        if (includeEmployeeId != null) {
            boolean found = false;
            for (Employee employee : employees) {
                if (employee.getId().equals(includeEmployeeId)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                employees.add(getById(includeEmployeeId));
                employees.sort(new java.util.Comparator<Employee>() {
                    @Override
                    public int compare(Employee a, Employee b) {
                        return a.getName().compareToIgnoreCase(b.getName());
                    }
                });
            }
        }
        return employees;
    }

    public List<Employee> search(String query) {
        String q = SearchQuery.normalize(query);
        if (q.isEmpty()) {
            return listAll();
        }
        return employeeRepository.searchByTerm(q);
    }

    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    public Optional<Employee> findByNic(String nic) {
        String normalized = NicNormalizer.normalize(nic);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        return employeeRepository.findByNicIgnoreCase(normalized);
    }

    @Transactional
    public Employee save(EmployeeForm form) {
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (form.getNic() == null || form.getNic().trim().isEmpty()) {
            throw new IllegalArgumentException("NIC is required.");
        }
        if (form.getJobStartDate() == null) {
            throw new IllegalArgumentException("Job start date is required.");
        }
        if (form.getStatus() == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        String nic = NicNormalizer.normalize(form.getNic());
        Employee employee;
        if (form.getId() != null) {
            employee = getById(form.getId());
            if (employeeRepository.existsByNicIgnoreCaseAndIdNot(nic, form.getId())) {
                throw new IllegalArgumentException("Another employee already uses this NIC.");
            }
        } else {
            if (employeeRepository.existsByNicIgnoreCase(nic)) {
                throw new IllegalArgumentException("An employee with this NIC already exists.");
            }
            employee = new Employee();
        }
        employee.setName(form.getName().trim());
        employee.setNic(nic);
        employee.setJobStartDate(form.getJobStartDate());
        employee.setStatus(form.getStatus());
        if (form.getImagePath() != null && !form.getImagePath().trim().isEmpty()) {
            employee.setEmployeeImagePath(form.getImagePath().trim());
        } else if (form.getId() == null) {
            employee.setEmployeeImagePath(EmployeeForm.DEFAULT_IMAGE_PATH);
        }
        cacheService.clearEmployeeCaches();
        return employeeRepository.save(employee);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        employeePaymentRepository.deleteByEmployeeId(id);
        cacheService.clearEmployeeCaches();
        employeeRepository.deleteById(id);
    }

    public EmployeeForm toForm(Employee employee) {
        EmployeeForm form = new EmployeeForm();
        form.setId(employee.getId());
        form.setName(employee.getName());
        form.setNic(employee.getNic());
        form.setJobStartDate(employee.getJobStartDate());
        form.setStatus(employee.getStatus());
        form.setImagePath(employee.getEmployeeImagePath());
        return form;
    }

}
