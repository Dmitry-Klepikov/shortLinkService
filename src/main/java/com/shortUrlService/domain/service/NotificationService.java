package com.shortUrlService.domain.service;

import com.shortUrlService.domain.model.ShortUrl;

import java.util.UUID;

public interface NotificationService {
    void notifyLinkExpired(UUID userId, ShortUrl url);
    void notifyLimitReached(UUID userId, ShortUrl url);
}