package com.carrental.service;

import com.carrental.web.dto.RentalOverdueAlert;
import com.carrental.web.dto.ServiceOverdueAlert;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

@Component
public class FleetAlertsSessionHelper {

    public static final String SERVICE_OVERDUE_ALERTS = "serviceOverdueAlerts";
    public static final String SERVICE_OVERDUE_COUNT = "serviceOverdueCount";
    public static final String RENTAL_OVERDUE_ALERTS = "rentalOverdueAlerts";
    public static final String RENTAL_OVERDUE_COUNT = "rentalOverdueCount";
    public static final String FLEET_NOTIFICATION_COUNT = "fleetNotificationCount";

    private final FleetServiceAlertService fleetServiceAlertService;
    private final RentalService rentalService;

    public FleetAlertsSessionHelper(
            FleetServiceAlertService fleetServiceAlertService,
            RentalService rentalService) {
        this.fleetServiceAlertService = fleetServiceAlertService;
        this.rentalService = rentalService;
    }

    public void refresh(HttpSession session) {
        if (session == null) {
            return;
        }
        List<ServiceOverdueAlert> serviceAlerts = fleetServiceAlertService.findServiceOverdue();
        List<RentalOverdueAlert> rentalAlerts = rentalService.findOverdueAlerts();
        session.setAttribute(SERVICE_OVERDUE_ALERTS, serviceAlerts);
        session.setAttribute(SERVICE_OVERDUE_COUNT, serviceAlerts.size());
        session.setAttribute(RENTAL_OVERDUE_ALERTS, rentalAlerts);
        session.setAttribute(RENTAL_OVERDUE_COUNT, rentalAlerts.size());
        session.setAttribute(FLEET_NOTIFICATION_COUNT, serviceAlerts.size() + rentalAlerts.size());
    }

    public void clear(HttpSession session) {
        if (session == null) {
            return;
        }
        session.setAttribute(SERVICE_OVERDUE_ALERTS, Collections.emptyList());
        session.setAttribute(SERVICE_OVERDUE_COUNT, 0);
        session.setAttribute(RENTAL_OVERDUE_ALERTS, Collections.emptyList());
        session.setAttribute(RENTAL_OVERDUE_COUNT, 0);
        session.setAttribute(FLEET_NOTIFICATION_COUNT, 0);
    }
}
