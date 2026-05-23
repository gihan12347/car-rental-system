package com.carrental.service;

import com.carrental.model.BlacklistedCustomer;
import com.carrental.model.Rental;
import com.carrental.repository.BlacklistedCustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BlacklistedCustomerService {

    private final BlacklistedCustomerRepository blacklistedCustomerRepository;

    public BlacklistedCustomerService(BlacklistedCustomerRepository blacklistedCustomerRepository) {
        this.blacklistedCustomerRepository = blacklistedCustomerRepository;
    }

    public boolean isBlacklisted(String customerContact) {
        String normalized = normalizeContact(customerContact);
        if (!StringUtils.hasText(normalized)) {
            return false;
        }
        return blacklistedCustomerRepository.findByCustomerContactIgnoreCase(normalized).isPresent();
    }

    public void ensureNotBlacklisted(String customerContact) {
        if (isBlacklisted(customerContact)) {
            throw new IllegalStateException(
                    "This customer is blacklisted and cannot be booked. Contact an administrator to review the blacklist.");
        }
    }

    @Transactional
    public void blacklistFromRental(Rental rental, String reason) {
        if (rental == null) {
            return;
        }
        String contact = normalizeContact(rental.getCustomerContact());
        if (!StringUtils.hasText(contact)) {
            throw new IllegalArgumentException("Customer phone is required to add a blacklist entry.");
        }
        if (blacklistedCustomerRepository.findByCustomerContactIgnoreCase(contact).isPresent()) {
            return;
        }
        BlacklistedCustomer entry = new BlacklistedCustomer();
        entry.setCustomerContact(contact);
        entry.setCustomerName(rental.getCustomerName() != null ? rental.getCustomerName().trim() : contact);
        entry.setReason(buildReason(reason, rental));
        blacklistedCustomerRepository.save(entry);
    }

    private static String buildReason(String reason, Rental rental) {
        if (StringUtils.hasText(reason)) {
            return reason.trim();
        }
        return "Blacklisted when completing rental #" + rental.getId();
    }

    private static String normalizeContact(String contact) {
        if (contact == null) {
            return "";
        }
        return contact.trim();
    }
}
