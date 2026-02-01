package com.shortUrlService.application;

import com.shortUrlService.domain.service.UrlShrinkDomainService;

import java.util.UUID;

public class UrlShrinkApplicationService {
    private final UrlShrinkDomainService domainService;

    public UrlShrinkApplicationService(UrlShrinkDomainService domainService) {
        this.domainService = domainService;
    }

    public UUID registerUser() {
        return UUID.randomUUID();
    }

    public String shortenUrl(UUID userId, String originalUrl, int maxClicks) {
        return domainService.createShortUrl(userId, originalUrl, maxClicks);
    }

    public String openShortUrl(String shortCode) {
        return domainService.resolveUrl(shortCode);
    }

    public void listUserUrls(UUID userId) {
        domainService.printUserUrls(userId);
    }

    public boolean updateUrlMaxClicks(UUID userId, String shortCode, int newMaxClicks) {
        return domainService.updateUrlMaxClicks(userId, shortCode, newMaxClicks);
    }

    public boolean extendUrlLifetime(UUID userId, String shortCode, int additionalDays) {
        return domainService.extendUrlLifetime(userId, shortCode, additionalDays);
    }

    public boolean deleteUrl(UUID userId, String shortCode) {
        return domainService.deleteUrl(userId, shortCode);
    }

    public void printUserStats(UUID userId) {
        domainService.printUserStats(userId);
    }
}
