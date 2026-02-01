package com.shortUrlService.domain.service;

import com.shortUrlService.domain.model.ShortUrl;
import com.shortUrlService.infrastructure.persistence.InMemoryUrlRepository;
import com.shortUrlService.infrastructure.shortening.ShortCodeGenerator;
import com.shortUrlService.config.AppConfig;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UrlShrinkDomainService {
    private final InMemoryUrlRepository repository;
    private final ShortCodeGenerator generator;
    private final NotificationService notificationService;

    public UrlShrinkDomainService(InMemoryUrlRepository repository,
                                  ShortCodeGenerator generator,
                                  NotificationService notificationService) {
        this.repository = repository;
        this.generator = generator;
        this.notificationService = notificationService;
    }

    public String createShortUrl(UUID userId, String originalUrl, int maxClicks) {
        // Валидация URL
        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("Некорректный URL: " + originalUrl);
        }

        // Валидация лимита кликов
        if (maxClicks <= 0) {
            throw new IllegalArgumentException("Лимит кликов должен быть положительным числом");
        }

        String shortCode = generator.generateUniqueCode(originalUrl, userId);

        while (repository.findByShortCode(shortCode).isPresent()) {
            shortCode = generator.generateUniqueCode(originalUrl + System.nanoTime(), userId);
        }

        ShortUrl shortUrl = ShortUrl.builder()
                .userId(userId)
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .maxClicks(maxClicks)
                .expiresAt(LocalDateTime.now().plus(AppConfig.getDefaultTtlDays(), ChronoUnit.DAYS))
                .build();

        repository.save(shortUrl);
        return AppConfig.getBaseUrl() + shortCode;
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String resolveUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                .map(url -> {
                    if (url.isActive()) {
                        url.incrementClick();
                        repository.save(url);

                        if (url.getClickCount() >= url.getMaxClicks()) {
                            notificationService.notifyLimitReached(url.getUserId(), url);
                        }

                        return url.getOriginalUrl();
                    } else {
                        if (LocalDateTime.now().isAfter(url.getExpiresAt())) {
                            notificationService.notifyLinkExpired(url.getUserId(), url);
                        } else if (url.getClickCount() >= url.getMaxClicks()) {
                            notificationService.notifyLimitReached(url.getUserId(), url);
                        }
                        return "Ссылка недоступна (истек срок или превышено количество кликов)";
                    }
                })
                .orElse("Ссылка не найдена");
    }

    public void printUserUrls(UUID userId) {
        repository.findByUserId(userId).forEach(url ->
                System.out.printf("Короткая: %s -> %s (кликов: %d/%d, активна: %s)%n",
                        AppConfig.getBaseUrl() + url.getShortCode(),
                        url.getOriginalUrl(),
                        url.getClickCount(),
                        url.getMaxClicks(),
                        url.isActive() ? "да" : "нет")
        );
    }

    public boolean updateUrlMaxClicks(UUID userId, String shortCode, int newMaxClicks) {
        if (newMaxClicks <= 0) {
            throw new IllegalArgumentException("Лимит кликов должен быть положительным числом");
        }

        return repository.findByShortCode(shortCode)
                .filter(url -> url.belongsToUser(userId))
                .map(url -> {
                    ShortUrl updatedUrl = ShortUrl.builder()
                            .userId(url.getUserId())
                            .originalUrl(url.getOriginalUrl())
                            .shortCode(url.getShortCode())
                            .maxClicks(newMaxClicks)
                            .expiresAt(url.getExpiresAt())
                            .clickCount(url.getClickCount())
                            .active(url.isActive())
                            .build();
                    repository.save(updatedUrl);
                    return true;
                })
                .orElse(false);
    }

    public boolean extendUrlLifetime(UUID userId, String shortCode, int additionalDays) {
        if (additionalDays <= 0) {
            throw new IllegalArgumentException("Количество дней должно быть положительным числом");
        }

        return repository.findByShortCode(shortCode)
                .filter(url -> url.belongsToUser(userId))
                .map(url -> {
                    ShortUrl updatedUrl = ShortUrl.builder()
                            .userId(url.getUserId())
                            .originalUrl(url.getOriginalUrl())
                            .shortCode(url.getShortCode())
                            .maxClicks(url.getMaxClicks())
                            .expiresAt(url.getExpiresAt().plusDays(additionalDays))
                            .clickCount(url.getClickCount())
                            .active(url.isActive())
                            .build();
                    repository.save(updatedUrl);
                    return true;
                })
                .orElse(false);
    }

    public boolean deleteUrl(UUID userId, String shortCode) {
        Optional<ShortUrl> urlOpt = repository.findByShortCode(shortCode);

        if (urlOpt.isPresent() && urlOpt.get().belongsToUser(userId)) {
            return repository.removeByShortCode(shortCode);
        }
        return false;
    }

    public void printUserStats(UUID userId) {
        List<ShortUrl> userUrls = repository.findByUserId(userId);
        if (userUrls.isEmpty()) {
            System.out.println("У вас нет активных ссылок");
            return;
        }

        int activeCount = 0;
        int expiredCount = 0;
        int totalClicks = 0;

        for (ShortUrl url : userUrls) {
            if (url.isActive()) {
                activeCount++;
            } else {
                expiredCount++;
            }
            totalClicks += url.getClickCount();
        }

        System.out.println("Статистика пользователя " + userId + ":");
        System.out.println("  Всего ссылок: " + userUrls.size());
        System.out.println("  Активных: " + activeCount);
        System.out.println("  Истекших: " + expiredCount);
        System.out.println("  Всего кликов: " + totalClicks);
        System.out.println("  Среднее кликов на ссылку: " +
                (userUrls.size() > 0 ? totalClicks / userUrls.size() : 0));
    }
}