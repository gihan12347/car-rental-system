package com.carrental.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Avoids the Whitelabel 403 page when the browser back button replays a stale form (invalid CSRF).
 */
public class FriendlyAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        if (response.isCommitted()) {
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
