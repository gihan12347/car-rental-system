package com.carrental.web;

import com.carrental.dashboard.DashboardAnalyticsService;
import com.carrental.dashboard.DashboardData;
import com.carrental.model.DashboardPeriod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    private final DashboardAnalyticsService dashboardAnalyticsService;
    private final ObjectMapper objectMapper;

    public DashboardController(
            DashboardAnalyticsService dashboardAnalyticsService,
            ObjectMapper objectMapper) {
        this.dashboardAnalyticsService = dashboardAnalyticsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/dashboard")
    public String home(
            @RequestParam(value = "period", required = false) String periodParam,
            Model model) throws JsonProcessingException {
        DashboardPeriod period = DashboardPeriod.fromParam(periodParam);
        DashboardData dashboard = dashboardAnalyticsService.build(period);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("dashboardPeriods", DashboardPeriod.values());
        model.addAttribute("chartsJson", objectMapper.writeValueAsString(dashboard));
        model.addAttribute("activeNav", "home");
        return "dashboard";
    }
}
