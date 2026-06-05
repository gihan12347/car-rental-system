package com.carrental.security;

import com.carrental.service.FleetAlertsSessionHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.carrental.util.AuthConstants.SESSION_USERNAME;
import static com.carrental.util.AuthConstants.SESSION_USER_ROLE;

@Component
public class FleetDeskAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final FleetAlertsSessionHelper fleetAlertsSessionHelper;

    public FleetDeskAuthenticationSuccessHandler(FleetAlertsSessionHelper fleetAlertsSessionHelper) {
        this.fleetAlertsSessionHelper = fleetAlertsSessionHelper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        HttpSession session = request.getSession();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        session.setAttribute(SESSION_USER_ROLE, role);
        session.setAttribute(SESSION_USERNAME, authentication.getName());
        fleetAlertsSessionHelper.refresh(session);

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
