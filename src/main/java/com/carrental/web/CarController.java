package com.carrental.web;

import com.carrental.car.CarDetailData;
import com.carrental.model.Car;
import com.carrental.model.CarStatus;
import com.carrental.model.MaintenanceRecord;
import com.carrental.model.MaintenanceType;
import com.carrental.model.Rental;
import com.carrental.service.CarDetailService;
import com.carrental.service.CarService;
import com.carrental.service.FleetServiceAlertService;
import com.carrental.service.MaintenanceRecordService;
import com.carrental.storage.CarImageStorageService;
import com.carrental.web.dto.MaintenanceForm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;
    private final CarImageStorageService carImageStorageService;
    private final CarDetailService carDetailService;
    private final MaintenanceRecordService maintenanceRecordService;
    private final FleetServiceAlertService fleetServiceAlertService;
    private final ObjectMapper objectMapper;

    public CarController(
            CarService carService,
            CarImageStorageService carImageStorageService,
            CarDetailService carDetailService,
            MaintenanceRecordService maintenanceRecordService,
            FleetServiceAlertService fleetServiceAlertService,
            ObjectMapper objectMapper) {
        this.carService = carService;
        this.carImageStorageService = carImageStorageService;
        this.carDetailService = carDetailService;
        this.maintenanceRecordService = maintenanceRecordService;
        this.fleetServiceAlertService = fleetServiceAlertService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("cars", carService.search(q));
        model.addAttribute("searchQuery", SearchQuery.normalize(q));
        model.addAttribute("activeNav", "cars");
        return "cars/list";
    }

    @GetMapping("/service-overdue")
    public String serviceOverdue(Model model) {
        model.addAttribute("alerts", fleetServiceAlertService.findServiceOverdue());
        model.addAttribute("activeNav", "service-overdue");
        return "cars/service-overdue";
    }

    @GetMapping("/new")
    public String newForm(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("openNewCarModal", Boolean.TRUE);
        return "redirect:/cars";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("modalCar") Car modalCar,
            BindingResult bindingResult,
            @RequestParam(value = "carImage", required = false) MultipartFile carImage,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("modalCar", modalCar);
            redirectAttributes.addFlashAttribute("openNewCarModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the New car form and try again.");
            return RedirectUtil.redirectToReferer(request, "/cars");
        }
        try {
            if (carImage != null && !carImage.isEmpty()) {
                modalCar.setImagePath(carImageStorageService.store(carImage));
            }
            carService.save(modalCar);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("modalCar", modalCar);
            redirectAttributes.addFlashAttribute("openNewCarModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return RedirectUtil.redirectToReferer(request, "/cars");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("modalCar", modalCar);
            redirectAttributes.addFlashAttribute("openNewCarModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", "Could not store image: " + e.getMessage());
            return RedirectUtil.redirectToReferer(request, "/cars");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("modalCar", modalCar);
            redirectAttributes.addFlashAttribute("openNewCarModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return RedirectUtil.redirectToReferer(request, "/cars");
        }
        redirectAttributes.addFlashAttribute("successMessage", "Car saved successfully.");
        return "redirect:/cars";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "rentalPage", defaultValue = "0") int rentalPage,
            @RequestParam(value = "maintenancePage", defaultValue = "0") int maintenancePage,
            Model model) throws JsonProcessingException {
        String monthParam = CarDetailService.parseMonth(month).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Page<Rental> rentalHistoryPage = carDetailService.getRentalHistory(id, rentalPage);
        Page<MaintenanceRecord> maintenanceHistoryPage =
                carDetailService.getMaintenanceHistory(id, maintenancePage);

        String redirect = carDetailPaginationRedirect(
                id, monthParam, rentalPage, maintenancePage, rentalHistoryPage, maintenanceHistoryPage);
        if (redirect != null) {
            return redirect;
        }

        CarDetailData detail = carDetailService.build(id, month);
        model.addAttribute("detail", detail);
        model.addAttribute("car", detail.getCar());
        model.addAttribute("serviceIntervalKm", MaintenanceRecordService.DEFAULT_SERVICE_INTERVAL_KM);
        model.addAttribute("maintenanceTypes", MaintenanceType.values());
        model.addAttribute("rentalHistoryPage", rentalHistoryPage);
        model.addAttribute("maintenanceHistoryPage", maintenanceHistoryPage);
        model.addAttribute("activeNav", "cars");
        Map<String, Object> chartPayload = new HashMap<String, Object>();
        chartPayload.put("incomeChart", detail.getIncomeChart());
        chartPayload.put("expenseChart", detail.getExpenseChart());
        chartPayload.put(
                "calendarDays",
                carDetailService.buildCalendarDayDetails(
                        CarDetailService.parseMonth(month),
                        detail.getRentals(),
                        detail.getMaintenanceRecords()));
        model.addAttribute("chartsJson", objectMapper.writeValueAsString(chartPayload));
        if (!model.containsAttribute("maintenanceForm")) {
            model.addAttribute("maintenanceForm", new MaintenanceForm());
        }
        return "cars/detail";
    }

    @PostMapping("/{id}/maintenance")
    public String addMaintenance(
            @PathVariable Long id,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam("maintenanceDate") String maintenanceDateRaw,
            @RequestParam("description") String description,
            @RequestParam("cost") String costRaw,
            @RequestParam(value = "maintenanceType", defaultValue = "OTHER") String maintenanceTypeRaw,
            @RequestParam(value = "nextServiceKm", required = false) String nextServiceKmRaw,
            RedirectAttributes redirectAttributes) {
        String monthParam = CarDetailService.parseMonth(month).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String redirectUrl = "redirect:/cars/" + id + "?month=" + monthParam;

        MaintenanceForm form = new MaintenanceForm();
        form.setDescription(description);

        LocalDate maintenanceDate = parseMaintenanceDate(maintenanceDateRaw);
        form.setMaintenanceDate(maintenanceDate != null ? maintenanceDate : LocalDate.now());

        java.math.BigDecimal cost = parseCost(costRaw);
        form.setCost(cost);

        MaintenanceType maintenanceType = parseMaintenanceType(maintenanceTypeRaw);
        form.setMaintenanceType(maintenanceType);

        Integer nextServiceKm = parseMileage(nextServiceKmRaw);
        form.setNextServiceKm(nextServiceKm);

        Car car = carService.getById(id);
        String validationError = validateMaintenanceInput(
                maintenanceDate, description, cost, maintenanceType, car, nextServiceKmRaw, nextServiceKm);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("maintenanceForm", form);
            redirectAttributes.addFlashAttribute("openMaintenanceModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", validationError);
            return redirectUrl;
        }
        try {
            maintenanceRecordService.create(id, maintenanceDate, description, cost, maintenanceType, nextServiceKm);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("maintenanceForm", form);
            redirectAttributes.addFlashAttribute("openMaintenanceModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return redirectUrl;
        } catch (DataAccessException e) {
            redirectAttributes.addFlashAttribute("maintenanceForm", form);
            redirectAttributes.addFlashAttribute("openMaintenanceModal", Boolean.TRUE);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not save maintenance record. Ensure the maintenance_records table exists (see db/maintenance_records.sql).");
            return redirectUrl;
        }
        if (maintenanceType == MaintenanceType.SERVICE) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Service record saved. Next service km updated on the vehicle.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance record saved.");
        }
        return redirectUrl;
    }

    private static LocalDate parseMaintenanceDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static java.math.BigDecimal parseCost(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return new java.math.BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static MaintenanceType parseMaintenanceType(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return MaintenanceType.OTHER;
        }
        try {
            return MaintenanceType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MaintenanceType.OTHER;
        }
    }

    private static Integer parseMileage(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String validateMaintenanceInput(
            LocalDate maintenanceDate,
            String description,
            java.math.BigDecimal cost,
            MaintenanceType maintenanceType,
            Car car,
            String nextServiceKmRaw,
            Integer nextServiceKm) {
        if (maintenanceDate == null) {
            return "Select a valid maintenance date.";
        }
        if (description == null || description.trim().isEmpty()) {
            return "Description is required.";
        }
        if (cost == null) {
            return "Enter a valid cost amount (numbers only, e.g. 150.00).";
        }
        if (cost.compareTo(java.math.BigDecimal.ZERO) < 0) {
            return "Cost must be zero or positive.";
        }
        if (maintenanceType != MaintenanceType.SERVICE) {
            return null;
        }
        if (nextServiceKmRaw != null && !nextServiceKmRaw.trim().isEmpty() && nextServiceKm == null) {
            return "Next service km must be a whole number, or leave it empty to use current odometer + "
                    + MaintenanceRecordService.DEFAULT_SERVICE_INTERVAL_KM + " km.";
        }
        if (nextServiceKm != null && nextServiceKm <= 0) {
            return "Next service km must be greater than zero.";
        }
        if (nextServiceKm != null && car.getMileageKm() != null && nextServiceKm < car.getMileageKm()) {
            return "Next service km must be at or above the vehicle odometer (" + car.getMileageKm() + " km).";
        }
        return null;
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("car", carService.getById(id));
        model.addAttribute("statuses", CarStatus.values());
        model.addAttribute("vehicleTypes", com.carrental.model.VehicleType.values());
        model.addAttribute("activeNav", "cars");
        return "cars/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("car") Car car,
            BindingResult bindingResult,
            @RequestParam(value = "carImage", required = false) MultipartFile carImage,
            Model model,
            RedirectAttributes redirectAttributes) {
        car.setId(id);
        model.addAttribute("statuses", CarStatus.values());
        model.addAttribute("vehicleTypes", com.carrental.model.VehicleType.values());
        if (bindingResult.hasErrors()) {
            return "cars/form";
        }
        try {
            Car existing = carService.getById(id);
            if (carImage != null && !carImage.isEmpty()) {
                car.setImagePath(carImageStorageService.store(carImage));
            } else {
                car.setImagePath(existing.getImagePath());
            }
            carService.save(car);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "cars/form";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Could not store image: " + e.getMessage());
            return "cars/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "cars/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Car updated.");
        return "redirect:/cars/" + id;
    }

    private static String carDetailPaginationRedirect(
            Long carId,
            String monthParam,
            int rentalPage,
            int maintenancePage,
            Page<Rental> rentalHistoryPage,
            Page<MaintenanceRecord> maintenanceHistoryPage) {
        int rentalTarget = rentalPage;
        int maintenanceTarget = maintenancePage;
        if (rentalHistoryPage.getTotalPages() > 0 && rentalPage >= rentalHistoryPage.getTotalPages()) {
            rentalTarget = rentalHistoryPage.getTotalPages() - 1;
        }
        if (maintenanceHistoryPage.getTotalPages() > 0
                && maintenancePage >= maintenanceHistoryPage.getTotalPages()) {
            maintenanceTarget = maintenanceHistoryPage.getTotalPages() - 1;
        }
        if (rentalTarget != rentalPage || maintenanceTarget != maintenancePage) {
            return "redirect:/cars/" + carId
                    + "?month=" + monthParam
                    + "&rentalPage=" + rentalTarget
                    + "&maintenancePage=" + maintenanceTarget;
        }
        return null;
    }

}
