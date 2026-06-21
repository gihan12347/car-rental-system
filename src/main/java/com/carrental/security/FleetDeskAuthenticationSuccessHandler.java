package com.carrental.security;

import com.carrental.service.FleetAlertsSessionHelper;
import com.carrental.service.UserSessionHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class FleetDeskAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final FleetAlertsSessionHelper fleetAlertsSessionHelper;
    private final UserSessionHelper userSessionHelper;

    public FleetDeskAuthenticationSuccessHandler(
            FleetAlertsSessionHelper fleetAlertsSessionHelper,
            UserSessionHelper userSessionHelper) {
        this.fleetAlertsSessionHelper = fleetAlertsSessionHelper;
        this.userSessionHelper = userSessionHelper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        HttpSession session = request.getSession();
        userSessionHelper.refresh(session, authentication.getName());
        fleetAlertsSessionHelper.refresh(session);

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
