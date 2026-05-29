package com.carrental.config;

import com.carrental.service.FleetServiceAlertService;
import com.carrental.service.RentalService;
import com.carrental.web.dto.RentalOverdueAlert;
import com.carrental.web.dto.ServiceOverdueAlert;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = "com.carrental.web")
public class FleetAlertsModelAdvice {

    private final FleetServiceAlertService fleetServiceAlertService;
    private final RentalService rentalService;

    public FleetAlertsModelAdvice(
            FleetServiceAlertService fleetServiceAlertService,
            RentalService rentalService) {
        this.fleetServiceAlertService = fleetServiceAlertService;
        this.rentalService = rentalService;
    }

    @ModelAttribute
    public void fleetAlerts(Model model) {
        List<ServiceOverdueAlert> serviceAlerts = fleetServiceAlertService.findServiceOverdue();
        List<RentalOverdueAlert> rentalAlerts = rentalService.findOverdueAlerts();
        model.addAttribute("serviceOverdueAlerts", serviceAlerts);
        model.addAttribute("serviceOverdueCount", serviceAlerts.size());
        model.addAttribute("rentalOverdueAlerts", rentalAlerts);
        model.addAttribute("rentalOverdueCount", rentalAlerts.size());
        model.addAttribute("fleetNotificationCount", serviceAlerts.size() + rentalAlerts.size());
    }
}
