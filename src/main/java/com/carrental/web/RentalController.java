package com.carrental.web;

import com.carrental.model.HireType;
import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;
import com.carrental.service.*;
import com.carrental.web.dto.AvailableCarOption;
import com.carrental.web.dto.CompleteRentalForm;
import com.carrental.web.dto.RentalForm;
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

import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final BlacklistedCustomerService blacklistedCustomerService;
    private final FleetAlertsSessionHelper fleetAlertsSessionHelper;

    public RentalController(RentalService rentalService, BlacklistedCustomerService blacklistedCustomerService, FleetAlertsSessionHelper fleetAlertsSessionHelper) {
        this.rentalService = rentalService;
        this.blacklistedCustomerService = blacklistedCustomerService;
        this.fleetAlertsSessionHelper = fleetAlertsSessionHelper;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "p", defaultValue = "0") int p,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {
        String normalizedQuery = SearchQuery.normalize(q);
        Page<Rental> rentalsPage = rentalService.searchAll(q, p, size);
        if (rentalsPage.getTotalPages() > 0 && p >= rentalsPage.getTotalPages()) {
            String redirect = "/rentals?p=" + (rentalsPage.getTotalPages() - 1) + "&size=" + rentalsPage.getSize();
            if (!normalizedQuery.isEmpty()) {
                redirect += "&q=" + normalizedQuery;
            }
            return "redirect:" + redirect;
        }
        model.addAttribute("rentalsPage", rentalsPage);
        model.addAttribute("rentals", rentalsPage.getContent());
        model.addAttribute("searchQuery", normalizedQuery);
        model.addAttribute("listMode", "all");
        model.addAttribute("activeOnly", false);
        model.addAttribute("activeNav", "rentals");
        return "rentals/list";
    }

    @GetMapping("/active")
    public String active(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("rentals", rentalService.searchActive(q));
        model.addAttribute("searchQuery", SearchQuery.normalize(q));
        model.addAttribute("listMode", "active");
        model.addAttribute("activeOnly", true);
        model.addAttribute("activeNav", "active");
        return "rentals/list";
    }

    @GetMapping("/overdue")
    public String overdue(@RequestParam(value = "q", required = false) String q, Model model, HttpSession session) {
        LocalDate today = LocalDate.now();
        fleetAlertsSessionHelper.refresh(session);
        model.addAttribute("rentals", rentalService.searchOverdue(q));
        model.addAttribute("searchQuery", SearchQuery.normalize(q));
        model.addAttribute("listMode", "overdue");
        model.addAttribute("activeOnly", false);
        model.addAttribute("today", today);
        model.addAttribute("activeNav", "overdue");
        return "rentals/list";
    }

    @GetMapping("/new")
    public String newForm(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("openNewHireModal", Boolean.TRUE);
        return "redirect:/rentals";
    }

    @GetMapping("/available-cars")
    @ResponseBody
    public List<AvailableCarOption> availableCars(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (end.isBefore(start)) {
            return Collections.emptyList();
        }
        return rentalService.listAvailableCarsForPeriod(start, end);
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("hireForm") RentalForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        if (!form.isValidPeriod()) {
            bindingResult.rejectValue("endDate", "rental.period", "End date/time must be after the start date/time.");
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("hireForm", form);
            redirectAttributes.addFlashAttribute("openNewHireModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the New hire form and try again.");
            return RedirectUtil.redirectToReferer(request, "/rentals");
        }
        try {
            Rental created = rentalService.createRental(
                    form.getCarId(),
                    form.getHireType(),
                    form.getStartDate(),
                    form.getStartTime(),
                    form.getEndDate(),
                    form.getEndTime(),
                    form.getCustomerName(),
                    form.getCustomerAddress(),
                    form.getCustomerContact(),
                    form.getCustomerIdNumber(),
                    form.getTravelLocation(),
                    form.getCurrentMileageKm());
            if (Boolean.TRUE.equals(created.getEmployeeHire())) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Rental booked for the selected dates. Vehicle odometer updated to "
                                + form.getCurrentMileageKm() + " km. Employee hire — no charge will apply.");
                return "redirect:/rentals/active";
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("hireForm", form);
            redirectAttributes.addFlashAttribute("openNewHireModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return RedirectUtil.redirectToReferer(request, "/rentals");
        }
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Rental booked for the selected dates. Vehicle odometer updated to "
                        + form.getCurrentMileageKm() + " km.");
        return "redirect:/rentals/active";
    }

    @GetMapping("/{id}/complete")
    public String completeForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Rental rental = rentalService.getByIdWithCar(id);
        if (rental.getRentalStatus() != RentalStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only active rentals can be completed.");
            return "redirect:/rentals";
        }
        populateCompletePage(model, rental, null);
        if (!model.containsAttribute("completeForm")) {
            model.addAttribute("completeForm", CompleteRentalForm.forActiveRental(rental));
        } else {
            addPriceBreakdown(model, rental, (CompleteRentalForm) model.getAttribute("completeForm"));
        }
        return "rentals/complete";
    }

    @PostMapping("/{id}/complete")
    public String complete(
            @PathVariable Long id,
            @Valid @ModelAttribute("completeForm") CompleteRentalForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes, HttpSession session) {
        form.setRentalId(id);
        Rental rental = rentalService.getByIdWithCar(id);
        populateCompletePage(model, rental, form);
        addPriceBreakdown(model, rental, form);
        if (bindingResult.hasErrors()) {
            return "rentals/complete";
        }
        try {
            boolean blacklist = Boolean.TRUE.equals(form.getBlacklistCustomer());
            Rental completed = rentalService.completeRental(
                    id,
                    form.getReturnDate(),
                    form.getReturnTime(),
                    form.getReturnMileageKm(),
                    Boolean.TRUE.equals(form.getDocumentReturned()),
                    blacklist,
                    form.getBlacklistReason(),
                    form.getDiscount(),
                    form.getCompletionComment());
            String chargeNote = Boolean.TRUE.equals(completed.getEmployeeHire())
                    ? "Total charged: 0.00 (employee hire)."
                    : "Total charged: " + completed.getTotalPrice() + ".";
            String success = "Rental completed. " + chargeNote
                    + " Vehicle odometer updated to " + form.getReturnMileageKm() + " km.";
            if (blacklist) {
                success += " Customer was added to the blacklist.";
            }
            redirectAttributes.addFlashAttribute("successMessage", success);
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "rentals/complete";
        }
        fleetAlertsSessionHelper.refresh(session);
        return "redirect:/rentals";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request, HttpSession session) {
        try {
            Rental rental = rentalService.cancelRental(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Rental cancelled. Vehicle "
                            + rental.getCar().getRegistrationNumber()
                            + " is available again.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        fleetAlertsSessionHelper.refresh(session);
        return RedirectUtil.redirectToReferer(request, "/rentals");
    }

    private void populateCompletePage(Model model, Rental rental, CompleteRentalForm form) {
        model.addAttribute("rental", rental);
        model.addAttribute("startMileageKm", rental.getCar().getMileageKm());
        model.addAttribute("pickupDate", RentalPeriodHelper.startDate(rental));
        model.addAttribute("pickupTime", RentalPeriodHelper.pickupTime(rental));
        model.addAttribute("pickupDateTime", RentalPeriodHelper.pickupDateTime(rental));
        model.addAttribute("plannedReturnDate", rental.getReturnDate());
        model.addAttribute("plannedReturnTime", rental.getReturnTime() != null
                ? rental.getReturnTime()
                : RentalPeriodHelper.pickupTime(rental));
        model.addAttribute("plannedReturnDateTime", RentalPeriodHelper.plannedReturnDateTime(rental));
        HireType hireType = rental.getHireType() != null ? rental.getHireType() : HireType.PER_DAY;
        model.addAttribute("hireType", hireType);
        model.addAttribute("dailyRate", RentalPricingHelper.effectiveDailyRate(rental.getCar(), hireType));
        model.addAttribute("hourRate", rental.getCar().getExtraPricePerHour());
        model.addAttribute("kmRate", rental.getCar().getExtraPricePerKm());
        model.addAttribute("freeKmPerDay", rental.getCar().getFreeKmPerDay() != null ? rental.getCar().getFreeKmPerDay() : 0);
        model.addAttribute("customerBlacklisted", blacklistedCustomerService.isBlacklisted(rental.getCustomerIdNumber()));
        model.addAttribute("employeeHire", Boolean.TRUE.equals(rental.getEmployeeHire()));
        model.addAttribute("billedPlannedPeriod", false);
        model.addAttribute("activeNav", "rentals");
        if (form != null) {
            model.addAttribute("completeForm", form);
        }
    }

    private static void addPriceBreakdown(Model model, Rental rental, CompleteRentalForm form) {
        if (form == null || form.getReturnDate() == null || form.getReturnTime() == null || form.getReturnMileageKm() == null) {
            return;
        }
        try {
            boolean employeeHire = Boolean.TRUE.equals(rental.getEmployeeHire());
            HireType hireType = rental.getHireType() != null ? rental.getHireType() : HireType.PER_DAY;
            LocalDateTime pickupDateTime = RentalPeriodHelper.pickupDateTime(rental);
            LocalDateTime plannedReturnDateTime = RentalPeriodHelper.plannedReturnDateTime(rental);
            LocalDateTime actualReturnDateTime = RentalDurationHelper.combine(form.getReturnDate(), form.getReturnTime());
            RentalPricingHelper.PriceBreakdown breakdown = employeeHire
                    ? RentalPricingHelper.calculateWaivedForCompletion(
                            rental.getCar(),
                            hireType,
                            pickupDateTime,
                            plannedReturnDateTime,
                            actualReturnDateTime,
                            form.getReturnMileageKm())
                    : RentalPricingHelper.calculateForCompletion(
                            rental.getCar(),
                            hireType,
                            pickupDateTime,
                            plannedReturnDateTime,
                            actualReturnDateTime,
                            form.getReturnMileageKm());
            model.addAttribute("priceBreakdown", breakdown);
            model.addAttribute("billedPlannedPeriod", RentalPricingHelper.usesPlannedPeriodPrice(
                    plannedReturnDateTime, actualReturnDateTime));
            BigDecimal discount = RentalService.normalizeCompletionDiscount(form.getDiscount());
            if (discount.compareTo(breakdown.getTotal()) > 0) {
                discount = breakdown.getTotal();
            }
            model.addAttribute("previewDiscount", discount);
            model.addAttribute("previewFinalTotal", breakdown.getTotal().subtract(discount));
        } catch (IllegalArgumentException ignored) {
            // Preview hidden until inputs are valid
        }
    }
}
