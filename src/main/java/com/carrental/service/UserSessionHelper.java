package com.carrental.service;

import com.carrental.model.AppRole;
import com.carrental.model.AppUser;
import com.carrental.repository.AppUserRepository;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

import static com.carrental.util.AuthConstants.SESSION_PROFILE_IMAGE;
import static com.carrental.util.AuthConstants.SESSION_USERNAME;
import static com.carrental.util.AuthConstants.SESSION_USER_ROLE;
import static com.carrental.util.AuthConstants.SESSION_USER_ROLE_LABEL;

@Component
public class UserSessionHelper {

    private final AppUserRepository appUserRepository;

    public UserSessionHelper(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public void refresh(HttpSession session, String username) {
        if (session == null || username == null || username.trim().isEmpty()) {
            return;
        }
        appUserRepository.findByUsername(username.trim()).ifPresent(user -> applyUser(session, user));
    }

    public void applyUser(HttpSession session, AppUser user) {
        if (session == null || user == null) {
            return;
        }
        AppRole role = user.getRole() != null ? user.getRole() : AppRole.USER;
        session.setAttribute(SESSION_USERNAME, user.getUsername());
        session.setAttribute(SESSION_USER_ROLE, "ROLE_" + role.name());
        session.setAttribute(SESSION_USER_ROLE_LABEL, role.getLabel());
        session.setAttribute(SESSION_PROFILE_IMAGE, AppUserService.resolveProfileImagePath(user));
    }
}
