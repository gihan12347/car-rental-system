package com.carrental.service;

import com.carrental.model.BlacklistedCustomer;
import com.carrental.model.Rental;
import com.carrental.repository.BlacklistedCustomerRepository;
import com.carrental.util.NicNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BlacklistedCustomerService {

    private final BlacklistedCustomerRepository blacklistedCustomerRepository;

    public BlacklistedCustomerService(BlacklistedCustomerRepository blacklistedCustomerRepository) {
        this.blacklistedCustomerRepository = blacklistedCustomerRepository;
    }

    public boolean isBlacklisted(String customerIdNumber) {
        String normalized = normalizeNic(customerIdNumber);
        if (!StringUtils.hasText(normalized)) {
            return false;
        }
        return blacklistedCustomerRepository.findByCustomerIdNumber(normalized).isPresent();
    }

    public void ensureNotBlacklisted(String customerIdNumber) {
        if (isBlacklisted(customerIdNumber)) {
            throw new IllegalStateException(
                    "This customer is blacklisted and cannot be booked. Contact an administrator to review the blacklist.");
        }
    }

    @Transactional
    public void blacklistFromRental(Rental rental, String reason) {
        if (rental == null) {
            return;
        }
        String nic = normalizeNic(rental.getCustomerIdNumber());
        if (!StringUtils.hasText(nic)) {
            throw new IllegalArgumentException("Customer NIC is required to add a blacklist entry.");
        }
        if (blacklistedCustomerRepository.findByCustomerIdNumber(nic).isPresent()) {
            return;
        }
        BlacklistedCustomer entry = new BlacklistedCustomer();
        entry.setCustomerIdNumber(nic);
        entry.setCustomerName(rental.getCustomerName() != null ? rental.getCustomerName().trim() : nic);
        entry.setReason(buildReason(reason, rental));
        blacklistedCustomerRepository.save(entry);
    }

    private static String buildReason(String reason, Rental rental) {
        if (StringUtils.hasText(reason)) {
            return reason.trim();
        }
        return "Blacklisted when completing rental #" + rental.getId();
    }

    private static String normalizeNic(String nic) {
        return NicNormalizer.normalize(nic);
    }
}
