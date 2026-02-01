package com.shortUrlService.domain.model;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

class ShortUrlTest {

    @Test
    void builder_CreatesValidShortUrl() {
        UUID userId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        ShortUrl url = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .maxClicks(10)
                .expiresAt(expiresAt)
                .build();

        Assert.assertNotNull(url.getId());
        Assert.assertEquals(userId, url.getUserId());
        Assert.assertEquals("https://example.com", url.getOriginalUrl());
        Assert.assertEquals("abc123", url.getShortCode());
        Assert.assertEquals(0, url.getClickCount());
        Assert.assertEquals(10, url.getMaxClicks());
        Assert.assertEquals(expiresAt, url.getExpiresAt());
        Assert.assertTrue(url.isActive());
    }

    @Test
    void incrementClick_OnActiveUrl_IncreasesClickCount() {
        ShortUrl url = ShortUrl.builder()
                .userId(UUID.randomUUID())
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        Assert.assertEquals(0, url.getClickCount());
        url.incrementClick();
        Assert.assertEquals(1, url.getClickCount());
    }

    @Test
    void isActive_WhenExpiredByTime_ReturnsFalse() {
        ShortUrl url = ShortUrl.builder()
                .userId(UUID.randomUUID())
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().minusSeconds(1)) // Уже истекло
                .build();

        Assert.assertFalse(url.isActive());
    }

    @Test
    void isActive_WhenExpiredByClicks_ReturnsFalse() {
        ShortUrl url = ShortUrl.builder()
                .userId(UUID.randomUUID())
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .maxClicks(3)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .clickCount(3) // Уже достигнут лимит
                .build();

        Assert.assertFalse(url.isActive());
    }

    @Test
    void isActive_WhenStillValid_ReturnsTrue() {
        ShortUrl url = ShortUrl.builder()
                .userId(UUID.randomUUID())
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .clickCount(5)
                .build();

        Assert.assertTrue(url.isActive());
    }

    @Test
    void belongsToUser_SameUser_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        ShortUrl url = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        Assert.assertTrue(url.belongsToUser(userId));
    }

    @Test
    void belongsToUser_DifferentUser_ReturnsFalse() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        ShortUrl url = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        Assert.assertFalse(url.belongsToUser(otherUserId));
    }

    @Test
    void equals_SameShortCode_ReturnsTrue() {
        UUID userId = UUID.randomUUID();

        ShortUrl url1 = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://example1.com")
                .shortCode("abc123")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        ShortUrl url2 = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://example2.com")
                .shortCode("abc123")
                .maxClicks(20)
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        Assert.assertEquals(url1, url2);
        Assert.assertEquals(url1.hashCode(), url2.hashCode());
    }

    @Test
    void equals_DifferentShortCode_ReturnsFalse() {
        UUID userId = UUID.randomUUID();

        ShortUrl url1 = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        ShortUrl url2 = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://example.com")
                .shortCode("xyz789")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        Assert.assertNotEquals(url1, url2);
    }
}