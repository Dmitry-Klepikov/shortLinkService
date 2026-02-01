package com.shortUrlService.infrastructure.notification;

import com.shortUrlService.domain.model.ShortUrl;
import com.shortUrlService.domain.service.NotificationService;

import java.util.UUID;

public class ConsoleNotificationService implements NotificationService {
    @Override
    public void notifyLinkExpired(UUID userId, ShortUrl url) {
        System.out.printf("[Уведомление для %s] Ссылка %s истекла по времени%n",
                userId, url.getShortCode());
    }

    @Override
    public void notifyLimitReached(UUID userId, ShortUrl url) {
        System.out.printf("[Уведомление для %s] Ссылка %s достигла лимита кликов (%d)%n",
                userId, url.getShortCode(), url.getMaxClicks());
    }
}