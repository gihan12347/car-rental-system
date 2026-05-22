package com.carrental.service;

import com.carrental.model.AppUser;
import com.carrental.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUser(String username, String rawPassword) {
        String normalized = normalizeUsername(username);
        if (appUserRepository.findByUsername(normalized).isPresent()) {
            throw new IllegalArgumentException("Username \"" + normalized + "\" is already taken.");
        }
        AppUser user = new AppUser();
        user.setUsername(normalized);
        user.setPassword(passwordEncoder.encode(rawPassword));
        appUserRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User account not found."));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
    }

    private static String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.trim();
    }
}
