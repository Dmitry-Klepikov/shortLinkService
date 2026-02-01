package com.shortUrlService.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ShortUrl {
    private final UUID id;
    private final UUID userId;
    private final String originalUrl;
    private final String shortCode;
    private int clickCount;
    private final int maxClicks;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private boolean active;

    private ShortUrl(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.originalUrl = builder.originalUrl;
        this.shortCode = builder.shortCode;
        this.clickCount = builder.clickCount;
        this.maxClicks = builder.maxClicks;
        this.createdAt = builder.createdAt;
        this.expiresAt = builder.expiresAt;
        this.active = builder.active;

        checkExpiration();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void incrementClick() {
        if (isActive()) {
            clickCount++;
        }
    }

    private void checkExpiration() {
        boolean notExpiredByTime = LocalDateTime.now().isBefore(expiresAt);
        boolean notExpiredByClicks = clickCount < maxClicks;
        this.active = notExpiredByTime && notExpiredByClicks;
    }

    public boolean isActive() {
        checkExpiration();
        return active;
    }

    public boolean belongsToUser(UUID userId) {
        return this.userId.equals(userId);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public int getClickCount() { return clickCount; }
    public int getMaxClicks() { return maxClicks; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortUrl that = (ShortUrl) o;
        return Objects.equals(shortCode, that.shortCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortCode);
    }

    @Override
    public String toString() {
        return String.format("ShortUrl{code=%s, clicks=%d/%d, expires=%s, active=%s}",
                shortCode, clickCount, maxClicks, expiresAt, active);
    }

    public static class Builder {
        UUID id = UUID.randomUUID();
        UUID userId;
        String originalUrl;
        String shortCode;
        int clickCount = 0;
        int maxClicks;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt;
        boolean active = true;

        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder originalUrl(String originalUrl) { this.originalUrl = originalUrl; return this; }
        public Builder shortCode(String shortCode) { this.shortCode = shortCode; return this; }
        public Builder maxClicks(int maxClicks) { this.maxClicks = maxClicks; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder clickCount(int clickCount) { this.clickCount = clickCount; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public ShortUrl build() { return new ShortUrl(this); }
    }
}