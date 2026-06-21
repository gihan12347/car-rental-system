package com.carrental.web;

import com.carrental.model.AppRole;
import com.carrental.model.AppUser;
import com.carrental.repository.AppUserRepository;
import com.carrental.service.AppUserService;
import com.carrental.service.UserSessionHelper;
import com.carrental.storage.ImageStorageService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AppUserService appUserService;
    private final AppUserRepository appUserRepository;
    private final ImageStorageService imageStorageService;
    private final UserSessionHelper userSessionHelper;

    public AccountController(
            AppUserService appUserService,
            AppUserRepository appUserRepository,
            ImageStorageService imageStorageService,
            UserSessionHelper userSessionHelper) {
        this.appUserService = appUserService;
        this.appUserRepository = appUserRepository;
        this.imageStorageService = imageStorageService;
        this.userSessionHelper = userSessionHelper;
    }

    @GetMapping
    public String account(Model model, Authentication authentication, HttpServletRequest request) {
        model.addAttribute("activeNav", "account");
        model.addAttribute("pageTitle", "Account");
        if (authentication != null) {
            userSessionHelper.refresh(request.getSession(), authentication.getName());
        }
        if (!model.containsAttribute("createUserForm")) {
            model.addAttribute("createUserForm", new CreateUserForm());
        }
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        model.addAttribute("appRoles", AppRole.values());
        return "account/index";
    }

    @PostMapping("/profile-photo")
    public String updateProfilePhoto(
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        String username = authentication != null ? authentication.getName() : null;
        if (username == null || username.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be signed in to update your profile photo.");
            return "redirect:/account";
        }
        if (profileImage == null || profileImage.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please choose a photo to upload.");
            return "redirect:/account";
        }
        try {
            AppUser user = appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User account not found."));
            imageStorageService.deleteIfPresent(user.getProfileImagePath());
            String storedPath = imageStorageService.storeProfileImage(profileImage);
            appUserService.updateProfileImage(username, storedPath);
            userSessionHelper.refresh(request.getSession(), username);
            redirectAttributes.addFlashAttribute("successMessage", "Profile photo updated.");
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/account";
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
            appUserService.createUser(form.getUsername(), form.getPassword(), form.getRole());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("createUserForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account";
        }
        AppRole role = form.getRole() != null ? form.getRole() : AppRole.USER;
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "User \"" + form.getUsername().trim() + "\" created with role " + role.getLabel() + ".");
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
