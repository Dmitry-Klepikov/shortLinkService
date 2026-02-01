package com.shortUrlService.infrastructure.persistence;

import com.shortUrlService.domain.model.ShortUrl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUrlRepository {
    private final Map<String, ShortUrl> byShortCode = new ConcurrentHashMap<>();
    private final Map<UUID, List<ShortUrl>> byUserId = new ConcurrentHashMap<>();

    public void save(ShortUrl shortUrl) {
        byShortCode.put(shortUrl.getShortCode(), shortUrl);

        List<ShortUrl> userUrls = byUserId.computeIfAbsent(
                shortUrl.getUserId(),
                k -> new ArrayList<>()
        );

        userUrls.removeIf(url -> url.getShortCode().equals(shortUrl.getShortCode()));
        userUrls.add(shortUrl);
    }

    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return Optional.ofNullable(byShortCode.get(shortCode));
    }

    public List<ShortUrl> findByUserId(UUID userId) {
        return new ArrayList<>(byUserId.getOrDefault(userId, Collections.emptyList()));
    }

    public void deleteExpired() {
        byShortCode.values().removeIf(url -> !url.isActive());
        byUserId.values().forEach(list -> list.removeIf(url -> !url.isActive()));
    }
}