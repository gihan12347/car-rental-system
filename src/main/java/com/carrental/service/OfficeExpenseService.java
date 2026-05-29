package com.carrental.service;

import com.carrental.model.OfficeExpense;
import com.carrental.repository.OfficeExpenseRepository;
import com.carrental.web.SearchQuery;
import com.carrental.web.dto.OfficeExpenseForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class OfficeExpenseService {

    public static final int OFFICE_EXPENSE_PAGE_SIZE = 10;

    private final OfficeExpenseRepository officeExpenseRepository;

    public OfficeExpenseService(OfficeExpenseRepository officeExpenseRepository) {
        this.officeExpenseRepository = officeExpenseRepository;
    }

    public Page<OfficeExpense> search(String query, int page, EmployeePaymentPeriodFilter filter) {
        Pageable pageable = PageRequest.of(Math.max(0, page), OFFICE_EXPENSE_PAGE_SIZE);
        String q = SearchQuery.normalize(query);
        LocalDate start = filter.getRange().getStart();
        LocalDate end = filter.getRange().getEnd();
        if (q.isEmpty()) {
            return officeExpenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(
                    start, end, pageable);
        }
        return officeExpenseRepository.searchByTermAndExpenseDateBetween(q, start, end, pageable);
    }

    public BigDecimal totalInPeriod(String query, EmployeePaymentPeriodFilter filter) {
        String q = SearchQuery.normalize(query);
        LocalDate start = filter.getRange().getStart();
        LocalDate end = filter.getRange().getEnd();
        if (q.isEmpty()) {
            return officeExpenseRepository.sumByExpenseDateBetween(start, end);
        }
        return officeExpenseRepository.sumByTermAndExpenseDateBetween(q, start, end);
    }

    public long countInPeriod(EmployeePaymentPeriodFilter filter) {
        LocalDate start = filter.getRange().getStart();
        LocalDate end = filter.getRange().getEnd();
        return officeExpenseRepository.countByExpenseDateBetween(start, end);
    }

    public OfficeExpense getById(Long id) {
        return officeExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Office expense not found: " + id));
    }

    @Transactional
    public OfficeExpense save(OfficeExpenseForm form) {
        if (form.getCategory() == null) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (form.getAmount() == null || form.getAmount().signum() < 0) {
            throw new IllegalArgumentException("Amount must be zero or positive.");
        }
        if (form.getExpenseDate() == null) {
            throw new IllegalArgumentException("Expense date is required.");
        }

        OfficeExpense expense;
        if (form.getId() != null) {
            expense = getById(form.getId());
        } else {
            expense = new OfficeExpense();
        }

        expense.setCategory(form.getCategory());
        expense.setAmount(form.getAmount());
        expense.setExpenseDate(form.getExpenseDate());
        expense.setReferenceNumber(trimToNull(form.getReferenceNumber()));
        expense.setNotes(trimToNull(form.getNotes()));
        return officeExpenseRepository.save(expense);
    }

    @Transactional
    public void delete(Long id) {
        OfficeExpense expense = getById(id);
        officeExpenseRepository.delete(expense);
    }

    public OfficeExpenseForm toForm(OfficeExpense expense) {
        OfficeExpenseForm form = new OfficeExpenseForm();
        form.setId(expense.getId());
        form.setCategory(expense.getCategory());
        form.setAmount(expense.getAmount());
        form.setExpenseDate(expense.getExpenseDate());
        form.setReferenceNumber(expense.getReferenceNumber());
        form.setNotes(expense.getNotes());
        return form;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
