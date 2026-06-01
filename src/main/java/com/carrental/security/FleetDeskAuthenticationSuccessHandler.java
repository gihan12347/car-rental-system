package com.carrental.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Redirects to the dashboard after login with no-store headers so Back does not reuse a stale page.
 */
public class FleetDeskAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public FleetDeskAuthenticationSuccessHandler(String defaultTargetUrl) {
        super(defaultTargetUrl);
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        FleetDeskLogoutSuccessHandler.applyNoStoreHeaders(response);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
