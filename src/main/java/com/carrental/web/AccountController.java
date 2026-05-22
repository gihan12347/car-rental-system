package com.carrental.web;

import com.carrental.service.AppUserService;
import com.carrental.web.dto.ChangePasswordForm;
import com.carrental.web.dto.CreateUserForm;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AppUserService appUserService;

    public AccountController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public String account(Model model, Authentication authentication) {
        model.addAttribute("activeNav", "account");
        model.addAttribute("pageTitle", "Account");
        if (!model.containsAttribute("createUserForm")) {
            model.addAttribute("createUserForm", new CreateUserForm());
        }
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        if (authentication != null) {
            model.addAttribute("currentUsername", authentication.getName());
        }
        return "account/index";
    }

    @PostMapping("/users")
    public String createUser(
            @Valid @ModelAttribute("createUserForm") CreateUserForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createUserForm", bindingResult);
            redirectAttributes.addFlashAttribute("createUserForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the new user form and try again.");
            return "redirect:/account";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("createUserForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", "Password and confirmation do not match.");
            return "redirect:/account";
        }
        try {
            appUserService.createUser(form.getUsername(), form.getPassword());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("createUserForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account";
        }
        redirectAttributes.addFlashAttribute("successMessage", "User \"" + form.getUsername().trim() + "\" created.");
        return "redirect:/account";
    }

    @PostMapping("/password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordForm", bindingResult);
            redirectAttributes.addFlashAttribute("changePasswordForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the change password form and try again.");
            return "redirect:/account";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("changePasswordForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirmation do not match.");
            return "redirect:/account";
        }
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || username.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be signed in to change your password.");
            return "redirect:/account";
        }
        try {
            appUserService.changePassword(username, form.getCurrentPassword(), form.getNewPassword());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("changePasswordForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Your password has been updated.");
        return "redirect:/account";
    }
}
