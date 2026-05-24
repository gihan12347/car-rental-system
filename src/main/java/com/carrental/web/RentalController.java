package com.carrental.web;

import com.carrental.model.Rental;
import com.carrental.model.RentalStatus;
import com.carrental.service.BlacklistedCustomerService;
import com.carrental.service.RentalPeriodHelper;
import com.carrental.service.RentalPricingHelper;
import com.carrental.service.RentalService;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final BlacklistedCustomerService blacklistedCustomerService;

    public RentalController(RentalService rentalService, BlacklistedCustomerService blacklistedCustomerService) {
        this.rentalService = rentalService;
        this.blacklistedCustomerService = blacklistedCustomerService;
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
    public String overdue(@RequestParam(value = "q", required = false) String q, Model model) {
        LocalDate today = LocalDate.now();
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
            bindingResult.rejectValue("endDate", "rental.period", "End date must be on or after the start date.");
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("hireForm", form);
            redirectAttributes.addFlashAttribute("openNewHireModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the New hire form and try again.");
            return RedirectUtil.redirectToReferer(request, "/rentals");
        }
        try {
            rentalService.createRental(
                    form.getCarId(),
                    form.getStartDate(),
                    form.getEndDate(),
                    form.getCustomerName(),
                    form.getCustomerAddress(),
                    form.getCustomerContact(),
                    form.getCustomerIdNumber(),
                    form.getTravelLocation());
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("hireForm", form);
            redirectAttributes.addFlashAttribute("openNewHireModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return RedirectUtil.redirectToReferer(request, "/rentals");
        }
        redirectAttributes.addFlashAttribute("successMessage", "Rental booked for the selected dates.");
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
            CompleteRentalForm form = new CompleteRentalForm();
            form.setRentalId(id);
            LocalDate defaultReturn = rental.getReturnDate() != null
                    ? rental.getReturnDate()
                    : LocalDate.now();
            form.setReturnDate(defaultReturn);
            model.addAttribute("completeForm", form);
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
            RedirectAttributes redirectAttributes) {
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
                    form.getReturnMileageKm(),
                    Boolean.TRUE.equals(form.getDocumentReturned()),
                    blacklist,
                    form.getBlacklistReason());
            String success = "Rental completed. Total charged: " + completed.getTotalPrice()
                    + ". Vehicle odometer updated to " + form.getReturnMileageKm() + " km.";
            if (blacklist) {
                success += " Customer was added to the blacklist.";
            }
            redirectAttributes.addFlashAttribute("successMessage", success);
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "rentals/complete";
        }
        return "redirect:/rentals";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
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
        return RedirectUtil.redirectToReferer(request, "/rentals");
    }

    private void populateCompletePage(Model model, Rental rental, CompleteRentalForm form) {
        model.addAttribute("rental", rental);
        model.addAttribute("startMileageKm", rental.getCar().getMileageKm());
        model.addAttribute("pickupDate", RentalPeriodHelper.startDate(rental));
        model.addAttribute("dailyRate", rental.getCar().getRentalPricePerDay());
        model.addAttribute("kmRate", rental.getCar().getExtraPricePerKm());
        model.addAttribute("freeKmPerDay", rental.getCar().getFreeKmPerDay() != null ? rental.getCar().getFreeKmPerDay() : 0);
        model.addAttribute("customerBlacklisted", blacklistedCustomerService.isBlacklisted(rental.getCustomerContact()));
        model.addAttribute("activeNav", "rentals");
        if (form != null) {
            model.addAttribute("completeForm", form);
        }
    }

    private static void addPriceBreakdown(Model model, Rental rental, CompleteRentalForm form) {
        if (form == null || form.getReturnDate() == null || form.getReturnMileageKm() == null) {
            return;
        }
        try {
            RentalPricingHelper.PriceBreakdown breakdown = RentalPricingHelper.calculate(
                    rental.getCar(),
                    RentalPeriodHelper.startDate(rental),
                    form.getReturnDate(),
                    form.getReturnMileageKm());
            model.addAttribute("priceBreakdown", breakdown);
        } catch (IllegalArgumentException ignored) {
            // Preview hidden until inputs are valid
        }
    }
}
