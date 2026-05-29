package com.carrental.web;

import com.carrental.model.EmployeePaymentPeriod;
import com.carrental.model.OfficeExpense;
import com.carrental.model.OfficeExpenseCategory;
import com.carrental.service.EmployeePaymentPeriodFilter;
import com.carrental.service.OfficeExpenseService;
import com.carrental.web.dto.OfficeExpenseForm;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;

@Controller
@RequestMapping("/office-expenses")
public class OfficeExpenseController {

    private final OfficeExpenseService officeExpenseService;

    public OfficeExpenseController(OfficeExpenseService officeExpenseService) {
        this.officeExpenseService = officeExpenseService;
    }

    @GetMapping
    public String index(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        String searchQuery = SearchQuery.normalize(q);
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());

        Page<OfficeExpense> expensesPage = officeExpenseService.search(searchQuery, page, filter);
        if (expensesPage.getTotalPages() > 0 && page >= expensesPage.getTotalPages()) {
            return redirectIndex(searchQuery, filter, expensesPage.getTotalPages() - 1);
        }

        model.addAttribute("expensesPage", expensesPage);
        model.addAttribute("expenses", expensesPage.getContent());
        model.addAttribute("totalExpenses", officeExpenseService.totalInPeriod(searchQuery, filter));
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("activeNav", "office-expenses");
        model.addAttribute("expenseCategories", OfficeExpenseCategory.values());
        addPeriodFilterToModel(model, filter);

        if (!model.containsAttribute("expenseForm")) {
            model.addAttribute("expenseForm", new OfficeExpenseForm());
        }

        return "office-expenses/index";
    }

    @GetMapping("/new")
    public String newExpenseForm(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("expenseForm", new OfficeExpenseForm());
        redirectAttributes.addFlashAttribute("openExpenseModal", Boolean.TRUE);
        return "redirect:/office-expenses";
    }

    @PostMapping
    public String create(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Valid @ModelAttribute("expenseForm") OfficeExpenseForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.expenseForm", bindingResult);
            redirectAttributes.addFlashAttribute("expenseForm", form);
            redirectAttributes.addFlashAttribute("openExpenseModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the expense form.");
            return redirectIndex(q, EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now()), page);
        }
        try {
            officeExpenseService.save(form);
            redirectAttributes.addFlashAttribute("successMessage", "Office expense recorded.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("expenseForm", form);
            redirectAttributes.addFlashAttribute("openExpenseModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return redirectIndex(q, EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now()), page);
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Valid @ModelAttribute("expenseForm") OfficeExpenseForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.expenseForm", bindingResult);
            redirectAttributes.addFlashAttribute("expenseForm", form);
            redirectAttributes.addFlashAttribute("openExpenseModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the expense form.");
            return redirectIndex(q, EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now()), page);
        }
        try {
            officeExpenseService.save(form);
            redirectAttributes.addFlashAttribute("successMessage", "Office expense updated.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("expenseForm", form);
            redirectAttributes.addFlashAttribute("openExpenseModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return redirectIndex(q, EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now()), page);
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {
        try {
            officeExpenseService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Office expense removed.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return redirectIndex(q, EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now()), page);
    }

    @GetMapping("/{id}/edit")
    public String editModal(
            @PathVariable Long id,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            RedirectAttributes redirectAttributes) {
        OfficeExpense expense = officeExpenseService.getById(id);
        redirectAttributes.addFlashAttribute("expenseForm", officeExpenseService.toForm(expense));
        redirectAttributes.addFlashAttribute("openExpenseModal", Boolean.TRUE);
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());
        return redirectIndex(q, filter, page);
    }

    private static void addPeriodFilterToModel(Model model, EmployeePaymentPeriodFilter filter) {
        model.addAttribute("paymentPeriod", filter.getPeriodParam());
        model.addAttribute("paymentPeriodLabel", filter.getRangeLabel());
        model.addAttribute("paymentFrom", filter.getFrom());
        model.addAttribute("paymentTo", filter.getTo());
        model.addAttribute("paymentPeriods", EmployeePaymentPeriod.values());
    }

    private static String redirectIndex(String q, EmployeePaymentPeriodFilter filter, int page) {
        String url = "redirect:/office-expenses?period=" + filter.getPeriodParam()
                + "&page=" + Math.max(0, page);
        if (filter.getFrom() != null && filter.getTo() != null) {
            url += "&from=" + filter.getFrom() + "&to=" + filter.getTo();
        }
        String normalized = SearchQuery.normalize(q);
        if (!normalized.isEmpty()) {
            url += "&q=" + normalized;
        }
        return url;
    }
}
