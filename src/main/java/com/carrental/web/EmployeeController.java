package com.carrental.web;

import com.carrental.model.Employee;
import com.carrental.model.EmployeePayment;
import com.carrental.model.EmployeePaymentPeriod;
import com.carrental.model.EmployeeStatus;
import com.carrental.model.PaymentType;
import com.carrental.model.Rental;
import com.carrental.service.EmployeePaymentPeriodFilter;
import com.carrental.service.EmployeePaymentService;
import com.carrental.service.EmployeeRentalService;
import com.carrental.service.EmployeeService;
import com.carrental.web.dto.EmployeeForm;
import com.carrental.web.dto.EmployeePaymentForm;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private static final String TAB_EMPLOYEES = "employees";
    private static final String TAB_PAYMENTS = "payments";
    private static final String TAB_RENTALS = "rentals";

    private final EmployeeService employeeService;
    private final EmployeePaymentService employeePaymentService;
    private final EmployeeRentalService employeeRentalService;

    public EmployeeController(
            EmployeeService employeeService,
            EmployeePaymentService employeePaymentService,
            EmployeeRentalService employeeRentalService) {
        this.employeeService = employeeService;
        this.employeePaymentService = employeePaymentService;
        this.employeeRentalService = employeeRentalService;
    }

    @GetMapping
    public String index(
            @RequestParam(value = "tab", defaultValue = TAB_EMPLOYEES) String tab,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "paymentPage", defaultValue = "0") int paymentPage,
            @RequestParam(value = "rentalPage", defaultValue = "0") int rentalPage,
            Model model) {
        String activeTab = resolveTab(tab);
        String searchQuery = SearchQuery.normalize(q);
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());

        model.addAttribute("tab", activeTab);
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("activeNav", "employees");
        model.addAttribute("employeeStatuses", EmployeeStatus.values());
        model.addAttribute("paymentTypes", PaymentType.values());
        addPaymentFilterToModel(model, filter);

        Long paymentEmployeeId = null;
        if (model.containsAttribute("paymentForm")) {
            EmployeePaymentForm paymentForm = (EmployeePaymentForm) model.asMap().get("paymentForm");
            if (paymentForm != null) {
                paymentEmployeeId = paymentForm.getEmployeeId();
            }
        }
        model.addAttribute("employeesForSelect", employeeService.listForPaymentSelect(paymentEmployeeId));

        if (TAB_PAYMENTS.equals(activeTab)) {
            Page<EmployeePayment> paymentsPage = employeePaymentService.searchAll(searchQuery, paymentPage, filter);
            if (paymentsPage.getTotalPages() > 0 && paymentPage >= paymentsPage.getTotalPages()) {
                return redirectPaymentsIndex(searchQuery, filter, paymentsPage.getTotalPages() - 1);
            }
            model.addAttribute("paymentsPage", paymentsPage);
            model.addAttribute("payments", paymentsPage.getContent());
            model.addAttribute("totalPaid", employeePaymentService.totalInPeriod(searchQuery, filter));
        } else if (TAB_RENTALS.equals(activeTab)) {
            Page<Rental> rentalsPage = employeeRentalService.listAllEmployeeHires(searchQuery, rentalPage, filter);
            if (rentalsPage.getTotalPages() > 0 && rentalPage >= rentalsPage.getTotalPages()) {
                return redirectRentalsIndex(searchQuery, filter, rentalsPage.getTotalPages() - 1);
            }
            model.addAttribute("rentalsPage", rentalsPage);
            model.addAttribute("rentals", rentalsPage.getContent());
            model.addAttribute("allStaffHiresCount", employeeRentalService.countAllEmployeeHires());
        } else {
            model.addAttribute("employees", employeeService.search(searchQuery));
        }

        if (!model.containsAttribute("employeeForm")) {
            model.addAttribute("employeeForm", new EmployeeForm());
        }
        if (!model.containsAttribute("paymentForm")) {
            model.addAttribute("paymentForm", new EmployeePaymentForm());
        }

        return "employees/index";
    }

    @GetMapping("/new")
    public String newEmployeeForm(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("employeeForm", new EmployeeForm());
        redirectAttributes.addFlashAttribute("openEmployeeModal", Boolean.TRUE);
        return "redirect:/employees?tab=employees";
    }

    @GetMapping("/lookup-nic")
    @ResponseBody
    public Map<String, Object> lookupNic(@RequestParam("nic") String nic) {
        Map<String, Object> body = new HashMap<String, Object>();
        return employeeService.findByNic(nic)
                .map(employee -> {
                    body.put("matched", Boolean.TRUE);
                    body.put("employeeId", employee.getId());
                    body.put("name", employee.getName());
                    body.put("nic", employee.getNic());
                    return body;
                })
                .orElseGet(() -> {
                    body.put("matched", Boolean.FALSE);
                    return body;
                });
    }

    @GetMapping("/{id}/rentals")
    public String employeeRentals(
            @PathVariable Long id,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "rentalPage", defaultValue = "0") int rentalPage,
            Model model) {
        Employee employee = employeeService.getById(id);
        String searchQuery = SearchQuery.normalize(q);
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());
        Page<Rental> rentalsPage = employeeRentalService.listForEmployee(id, searchQuery, rentalPage, filter);
        if (rentalsPage.getTotalPages() > 0 && rentalPage >= rentalsPage.getTotalPages()) {
            return redirectEmployeeRentals(id, searchQuery, filter, rentalsPage.getTotalPages() - 1);
        }
        model.addAttribute("employee", employee);
        model.addAttribute("rentalsPage", rentalsPage);
        model.addAttribute("rentals", rentalsPage.getContent());
        model.addAttribute("hireCount", employeeRentalService.countForEmployee(id));
        model.addAttribute("hireCountInPeriod", employeeRentalService.countForEmployeeInPeriod(id, filter));
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("activeNav", "employees");
        addPaymentFilterToModel(model, filter);
        return "employees/rentals";
    }

    private static String redirectEmployeeRentals(
            Long employeeId,
            String q,
            EmployeePaymentPeriodFilter filter,
            int rentalPage) {
        String url = "redirect:/employees/" + employeeId + "/rentals?period=" + filter.getPeriodParam()
                + "&rentalPage=" + Math.max(0, rentalPage);
        if (filter.getFrom() != null && filter.getTo() != null) {
            url += "&from=" + filter.getFrom() + "&to=" + filter.getTo();
        }
        String normalized = SearchQuery.normalize(q);
        if (!normalized.isEmpty()) {
            url += "&q=" + normalized;
        }
        return url;
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "paymentPage", defaultValue = "0") int paymentPage,
            Model model) {
        Employee employee = employeeService.getById(id);
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());

        Page<EmployeePayment> paymentsPage = employeePaymentService.listForEmployee(id, paymentPage, filter);
        if (paymentsPage.getTotalPages() > 0 && paymentPage >= paymentsPage.getTotalPages()) {
            return redirectEmployeeDetail(id, filter, paymentsPage.getTotalPages() - 1, null);
        }

        model.addAttribute("employee", employee);
        model.addAttribute("paymentsPage", paymentsPage);
        model.addAttribute("payments", paymentsPage.getContent());
        model.addAttribute("totalPaid", employeePaymentService.totalForEmployeeInPeriod(id, filter));
        model.addAttribute("hireCount", employeeRentalService.countForEmployee(id));
        model.addAttribute("activeNav", "employees");
        model.addAttribute("paymentTypes", PaymentType.values());
        model.addAttribute("employeesForSelect", employeeService.listForPaymentSelect(id));
        addPaymentFilterToModel(model, filter);

        EmployeePaymentForm paymentForm = new EmployeePaymentForm();
        paymentForm.setEmployeeId(id);
        if (model.containsAttribute("paymentForm")) {
            paymentForm = (EmployeePaymentForm) model.asMap().get("paymentForm");
        }
        model.addAttribute("paymentForm", paymentForm);

        return "employees/detail";
    }

    @PostMapping
    public String createEmployee(
            @RequestParam(value = "tab", defaultValue = TAB_EMPLOYEES) String tab,
            @RequestParam(value = "q", required = false) String q,
            @Valid @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.employeeForm", bindingResult);
            redirectAttributes.addFlashAttribute("employeeForm", form);
            redirectAttributes.addFlashAttribute("openEmployeeModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the employee form.");
            return redirectIndex(tab, q, null, null, null, 0);
        }
        try {
            employeeService.save(form);
            redirectAttributes.addFlashAttribute("successMessage", "Employee saved.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("employeeForm", form);
            redirectAttributes.addFlashAttribute("openEmployeeModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return redirectIndex(TAB_EMPLOYEES, q, null, null, null, 0);
    }

    @PostMapping("/{id}")
    public String updateEmployee(
            @PathVariable Long id,
            @RequestParam(value = "tab", defaultValue = TAB_EMPLOYEES) String tab,
            @RequestParam(value = "q", required = false) String q,
            @Valid @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.employeeForm", bindingResult);
            redirectAttributes.addFlashAttribute("employeeForm", form);
            redirectAttributes.addFlashAttribute("openEmployeeModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the employee form.");
            return redirectIndex(tab, q, null, null, null, 0);
        }
        try {
            employeeService.save(form);
            redirectAttributes.addFlashAttribute("successMessage", "Employee updated.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("employeeForm", form);
            redirectAttributes.addFlashAttribute("openEmployeeModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return redirectIndex(TAB_EMPLOYEES, q, null, null, null, 0);
    }

    @PostMapping("/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            @RequestParam(value = "q", required = false) String q,
            RedirectAttributes redirectAttributes) {
        try {
            employeeService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Employee and related payments removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not remove employee: " + e.getMessage());
        }
        return redirectIndex(TAB_EMPLOYEES, q, null, null, null, 0);
    }

    @GetMapping("/{id}/edit")
    public String editEmployeeModal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.getById(id);
        redirectAttributes.addFlashAttribute("employeeForm", employeeService.toForm(employee));
        redirectAttributes.addFlashAttribute("openEmployeeModal", Boolean.TRUE);
        return "redirect:/employees";
    }

    @PostMapping("/payments")
    public String createPayment(
            @RequestParam(value = "tab", defaultValue = TAB_PAYMENTS) String tab,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "paymentPage", defaultValue = "0") int paymentPage,
            @RequestParam(value = "returnEmployeeId", required = false) Long returnEmployeeId,
            @Valid @ModelAttribute("paymentForm") EmployeePaymentForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.paymentForm", bindingResult);
            redirectAttributes.addFlashAttribute("paymentForm", form);
            redirectAttributes.addFlashAttribute("openPaymentModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the payment form.");
            return redirectAfterPayment(returnEmployeeId, form.getEmployeeId(), tab, q, period, from, to, paymentPage);
        }
        try {
            employeePaymentService.save(form);
            redirectAttributes.addFlashAttribute("successMessage", "Payment recorded.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("paymentForm", form);
            redirectAttributes.addFlashAttribute("openPaymentModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return redirectAfterPayment(returnEmployeeId, form.getEmployeeId(), tab, q, period, from, to, paymentPage);
    }

    @PostMapping("/payments/{id}")
    public String updatePayment(
            @PathVariable Long id,
            @RequestParam(value = "tab", defaultValue = TAB_PAYMENTS) String tab,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "paymentPage", defaultValue = "0") int paymentPage,
            @RequestParam(value = "returnEmployeeId", required = false) Long returnEmployeeId,
            @Valid @ModelAttribute("paymentForm") EmployeePaymentForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.paymentForm", bindingResult);
            redirectAttributes.addFlashAttribute("paymentForm", form);
            redirectAttributes.addFlashAttribute("openPaymentModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the payment form.");
            return redirectAfterPayment(returnEmployeeId, form.getEmployeeId(), tab, q, period, from, to, paymentPage);
        }
        try {
            employeePaymentService.save(form);
            redirectAttributes.addFlashAttribute("successMessage", "Payment updated.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("paymentForm", form);
            redirectAttributes.addFlashAttribute("openPaymentModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return redirectAfterPayment(returnEmployeeId, form.getEmployeeId(), tab, q, period, from, to, paymentPage);
    }

    @PostMapping("/payments/{id}/delete")
    public String deletePayment(
            @PathVariable Long id,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "paymentPage", defaultValue = "0") int paymentPage,
            @RequestParam(value = "returnEmployeeId", required = false) Long returnEmployeeId,
            RedirectAttributes redirectAttributes) {
        Long employeeId = returnEmployeeId;
        try {
            EmployeePayment payment = employeePaymentService.getById(id);
            if (employeeId == null) {
                employeeId = payment.getEmployee().getId();
            }
            employeePaymentService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Payment removed.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        if (employeeId != null) {
            EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());
            return redirectEmployeeDetail(employeeId, filter, paymentPage, q);
        }
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());
        return redirectIndex(TAB_PAYMENTS, q, filter.getPeriodParam(), filter.getFrom(), filter.getTo(), paymentPage);
    }

    @GetMapping("/payments/{id}/edit")
    public String editPaymentModal(
            @PathVariable Long id,
            @RequestParam(value = "returnEmployeeId", required = false) Long returnEmployeeId,
            @RequestParam(value = "period", defaultValue = "ALL_TIME") String period,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "paymentPage", defaultValue = "0") int paymentPage,
            RedirectAttributes redirectAttributes) {
        EmployeePayment payment = employeePaymentService.getById(id);
        EmployeePaymentForm form = employeePaymentService.toForm(payment);
        Long employeeId = returnEmployeeId != null ? returnEmployeeId : payment.getEmployee().getId();
        redirectAttributes.addFlashAttribute("paymentForm", form);
        redirectAttributes.addFlashAttribute("openPaymentModal", Boolean.TRUE);
        if (employeeId != null) {
            EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());
            return redirectEmployeeDetail(employeeId, filter, paymentPage, null);
        }
        redirectAttributes.addFlashAttribute("employeesForSelect",
                employeeService.listForPaymentSelect(form.getEmployeeId()));
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());
        return redirectIndex(TAB_PAYMENTS, null, filter.getPeriodParam(), filter.getFrom(), filter.getTo(), paymentPage);
    }

    private static String resolveTab(String tab) {
        if (TAB_PAYMENTS.equals(tab)) {
            return TAB_PAYMENTS;
        }
        if (TAB_RENTALS.equals(tab)) {
            return TAB_RENTALS;
        }
        return TAB_EMPLOYEES;
    }

    private static String redirectRentalsIndex(String q, EmployeePaymentPeriodFilter filter, int rentalPage) {
        String url = "redirect:/employees?tab=rentals&period=" + filter.getPeriodParam()
                + "&rentalPage=" + Math.max(0, rentalPage);
        if (filter.getFrom() != null && filter.getTo() != null) {
            url += "&from=" + filter.getFrom() + "&to=" + filter.getTo();
        }
        String normalized = SearchQuery.normalize(q);
        if (!normalized.isEmpty()) {
            url += "&q=" + normalized;
        }
        return url;
    }

    private static void addPaymentFilterToModel(Model model, EmployeePaymentPeriodFilter filter) {
        model.addAttribute("paymentPeriod", filter.getPeriodParam());
        model.addAttribute("paymentPeriodLabel", filter.getRangeLabel());
        model.addAttribute("paymentFrom", filter.getFrom());
        model.addAttribute("paymentTo", filter.getTo());
        model.addAttribute("paymentPeriods", EmployeePaymentPeriod.values());
    }

    private static String redirectAfterPayment(
            Long returnEmployeeId,
            Long formEmployeeId,
            String tab,
            String q,
            String period,
            LocalDate from,
            LocalDate to,
            int paymentPage) {
        Long employeeId = returnEmployeeId != null ? returnEmployeeId : formEmployeeId;
        if (employeeId != null) {
            EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());
            return redirectEmployeeDetail(employeeId, filter, paymentPage, q);
        }
        EmployeePaymentPeriodFilter filter = EmployeePaymentPeriodFilter.resolve(period, from, to, LocalDate.now());
        return redirectIndex(tab, q, filter.getPeriodParam(), filter.getFrom(), filter.getTo(), paymentPage);
    }

    private static String redirectEmployeeDetail(
            Long employeeId,
            EmployeePaymentPeriodFilter filter,
            int paymentPage,
            String q) {
        String url = "redirect:/employees/" + employeeId
                + "?period=" + filter.getPeriodParam()
                + "&paymentPage=" + Math.max(0, paymentPage);
        if (filter.getFrom() != null && filter.getTo() != null) {
            url += "&from=" + filter.getFrom() + "&to=" + filter.getTo();
        }
        return url;
    }

    private static String redirectPaymentsIndex(String q, EmployeePaymentPeriodFilter filter, int paymentPage) {
        return redirectIndex(TAB_PAYMENTS, q, filter.getPeriodParam(), filter.getFrom(), filter.getTo(), paymentPage);
    }

    private static String redirectIndex(
            String tab,
            String q,
            String period,
            LocalDate from,
            LocalDate to,
            int paymentPage) {
        String url = "redirect:/employees?tab=" + tab;
        if (period != null) {
            url += "&period=" + period;
        }
        if (from != null && to != null) {
            url += "&from=" + from + "&to=" + to;
        }
        if (paymentPage > 0) {
            url += "&paymentPage=" + paymentPage;
        }
        String normalized = SearchQuery.normalize(q);
        if (!normalized.isEmpty()) {
            url += "&q=" + normalized;
        }
        return url;
    }
}
