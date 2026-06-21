package com.carrental.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void clearEmployeeCaches() {
        clearCache("employeesAllSorted");
        clearCache("employeesActiveByStatus");
        clearCache("employeesByNic");
        clearCache("employeesSearch");
    }

    public void clearFleetCaches() {
        clearCache("carsByStatus");
        clearCache("carsAllSorted");
        clearCache("carsServiceOverdue");
        clearCache("carsSearch");
    }

    private void clearCache(String cacheName) {
        if (cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).clear();
        }
    }
}
