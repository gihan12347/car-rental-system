package com.carrental.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lets the login page detect an active server session (e.g. after Back from dashboard)
 * even when the browser shows a cached HTML copy of /login.
 */
@RestController
public class AuthStatusController {

    @GetMapping("/api/auth/status")
    public Map<String, Object> status(Authentication authentication) {
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("authenticated", authenticated);
        if (authenticated) {
            body.put("landingUrl", "/dashboard");
        }
        return body;
    }
}
