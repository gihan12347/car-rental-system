package com.carrental.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class CsrfModelAdvice {

    @ModelAttribute
    public void exposeCsrfToken(HttpServletRequest request, Model model) {
        if (model.containsAttribute("_csrf")) {
            return;
        }
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            token = (CsrfToken) request.getAttribute("_csrf");
        }
        if (token != null) {
            model.addAttribute("_csrf", token);
        }
    }
}
