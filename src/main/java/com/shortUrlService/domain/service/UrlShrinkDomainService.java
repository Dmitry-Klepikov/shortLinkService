package com.shortUrlService.domain.service;

import com.shortUrlService.domain.model.ShortUrl;
import com.shortUrlService.infrastructure.persistence.InMemoryUrlRepository;
import com.shortUrlService.infrastructure.shortening.ShortCodeGenerator;
import com.shortUrlService.config.AppConfig;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class UrlShrinkDomainService {
    private final InMemoryUrlRepository repository;
    private final ShortCodeGenerator generator;

    public UrlShrinkDomainService(InMemoryUrlRepository repository,
                                  ShortCodeGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    public String createShortUrl(UUID userId, String originalUrl, int maxClicks) {
        String shortCode = generator.generateUniqueCode(originalUrl, userId);

        while (repository.findByShortCode(shortCode)
                .filter(url -> url.getUserId().equals(userId)).isPresent()) {
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

    public String resolveUrl(String shortCode) {
        System.out.println(repository.findByShortCode(shortCode));
        return repository.findByShortCode(shortCode)
                .map(url -> {
                    if (url.isActive()) {
                        url.incrementClick();
                        return url.getOriginalUrl();
                    } else {
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
}