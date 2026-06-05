package com.carrental.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FriendlyAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        String redirect = isCsrfFailure(accessDeniedException)
                ? "/login?expired=1"
                : "/login?denied=1";
        response.sendRedirect(request.getContextPath() + redirect);
    }

    private static boolean isCsrfFailure(AccessDeniedException ex) {
        if (ex instanceof InvalidCsrfTokenException || ex instanceof MissingCsrfTokenException) {
            return true;
        }
        Throwable cause = ex.getCause();
        return cause instanceof InvalidCsrfTokenException || cause instanceof MissingCsrfTokenException;
    }
}
