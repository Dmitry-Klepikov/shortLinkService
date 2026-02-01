package com.shortUrlService.infrastructure.persistence;

import com.shortUrlService.domain.model.ShortUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUrlRepositoryTest {

    private InMemoryUrlRepository repository;
    private UUID userId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUrlRepository();
        userId = UUID.randomUUID();
    }

    @Test
    void save_NewShortUrl_SavesSuccessfully() {
        ShortUrl url = createTestShortUrl("abc123");

        repository.save(url);

        Optional<ShortUrl> found = repository.findByShortCode("abc123");
        assertTrue(found.isPresent());
        assertEquals(url, found.get());
    }

    @Test
    void save_UpdateExisting_ReplacesOld() {
        ShortUrl url1 = createTestShortUrl("abc123");
        url1 = ShortUrl.builder()
                .userId(url1.getUserId())
                .originalUrl(url1.getOriginalUrl())
                .shortCode(url1.getShortCode())
                .maxClicks(10)
                .expiresAt(url1.getExpiresAt())
                .clickCount(5)
                .build();

        repository.save(url1);

        ShortUrl url2 = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://updated.com")
                .shortCode("abc123") // Тот же код
                .maxClicks(20)
                .expiresAt(LocalDateTime.now().plusDays(2))
                .clickCount(10)
                .build();

        repository.save(url2);

        Optional<ShortUrl> found = repository.findByShortCode("abc123");
        assertTrue(found.isPresent());
        assertEquals(20, found.get().getMaxClicks());
        assertEquals(10, found.get().getClickCount());
        assertEquals("https://updated.com", found.get().getOriginalUrl());
    }

    @Test
    void findByShortCode_ExistingCode_ReturnsUrl() {
        ShortUrl url = createTestShortUrl("abc123");
        repository.save(url);

        Optional<ShortUrl> found = repository.findByShortCode("abc123");

        assertTrue(found.isPresent());
        assertEquals(url, found.get());
    }

    @Test
    void findByUserId_UserWithUrls_ReturnsAllUrls() {
        ShortUrl url1 = createTestShortUrl("abc123");
        ShortUrl url2 = createTestShortUrl("def456");

        repository.save(url1);
        repository.save(url2);

        List<ShortUrl> userUrls = repository.findByUserId(userId);

        assertEquals(2, userUrls.size());
        assertTrue(userUrls.contains(url1));
        assertTrue(userUrls.contains(url2));
    }

    @Test
    void findByUserId_UserWithoutUrls_ReturnsEmptyList() {
        UUID otherUserId = UUID.randomUUID();

        List<ShortUrl> userUrls = repository.findByUserId(otherUserId);

        assertTrue(userUrls.isEmpty());
    }

    @Test
    void deleteExpired_RemovesExpiredUrls() {
        ShortUrl activeUrl = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://active.com")
                .shortCode("active")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        ShortUrl expiredByTime = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://expired-time.com")
                .shortCode("expiredTime")
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .build();

        ShortUrl expiredByClicks = ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://expired-clicks.com")
                .shortCode("expiredClicks")
                .maxClicks(3)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .clickCount(3)
                .build();

        repository.save(activeUrl);
        repository.save(expiredByTime);
        repository.save(expiredByClicks);

        repository.deleteExpired();

        assertTrue(repository.findByShortCode("active").isPresent());
        assertFalse(repository.findByShortCode("expiredTime").isPresent());
        assertFalse(repository.findByShortCode("expiredClicks").isPresent());
    }

    @Test
    void removeByShortCode_ExistingCode_RemovesUrl() {
        ShortUrl url = createTestShortUrl("abc123");
        repository.save(url);

        boolean removed = repository.removeByShortCode("abc123");

        assertTrue(removed);
        assertFalse(repository.findByShortCode("abc123").isPresent());

        List<ShortUrl> userUrls = repository.findByUserId(userId);
        assertTrue(userUrls.isEmpty());
    }

    @Test
    void removeByShortCode_NonExistentCode_ReturnsFalse() {
        boolean removed = repository.removeByShortCode("nonexistent");

        assertFalse(removed);
    }

    private ShortUrl createTestShortUrl(String shortCode) {
        return ShortUrl.builder()
                .userId(userId)
                .originalUrl("https://example.com")
                .shortCode(shortCode)
                .maxClicks(10)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }
}