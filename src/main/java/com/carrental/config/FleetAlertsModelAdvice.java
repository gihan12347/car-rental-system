package com.carrental.config;

import com.carrental.service.FleetServiceAlertService;
import com.carrental.web.dto.ServiceOverdueAlert;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = "com.carrental.web")
public class FleetAlertsModelAdvice {

    private final FleetServiceAlertService fleetServiceAlertService;

    public FleetAlertsModelAdvice(FleetServiceAlertService fleetServiceAlertService) {
        this.fleetServiceAlertService = fleetServiceAlertService;
    }

    @ModelAttribute
    public void fleetAlerts(Model model) {
        List<ServiceOverdueAlert> alerts = fleetServiceAlertService.findServiceOverdue();
        model.addAttribute("serviceOverdueAlerts", alerts);
        model.addAttribute("serviceOverdueCount", alerts.size());
    }
}
