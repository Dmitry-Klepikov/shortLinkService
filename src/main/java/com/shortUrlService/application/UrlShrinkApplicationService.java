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
}
