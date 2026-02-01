package com.shortUrlService.domain.service;

import com.shortUrlService.infrastructure.persistence.InMemoryUrlRepository;
import com.shortUrlService.infrastructure.shortening.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShrinkDomainServiceTest {

    private UrlShrinkDomainService service;
    private InMemoryUrlRepository repository;
    private ShortCodeGenerator generator;

    @Mock
    private NotificationService notificationService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUrlRepository();
        generator = new ShortCodeGenerator();
        service = new UrlShrinkDomainService(repository, generator, notificationService);
        testUserId = UUID.randomUUID();
    }

    @Test
    void createShortUrl_ValidUrl_ReturnsShortUrl() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);

        assertNotNull(shortUrl);
        assertTrue(shortUrl.startsWith("http://clck.ru/"));
        assertEquals(6, shortUrl.substring(shortUrl.lastIndexOf("/") + 1).length());
    }

    @Test
    void createShortUrl_InvalidUrl_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.createShortUrl(testUserId, "not-a-url", 10);
        });
    }

    @Test
    void createShortUrl_InvalidMaxClicks_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.createShortUrl(testUserId, "https://example.com", 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            service.createShortUrl(testUserId, "https://example.com", -5);
        });
    }

    @Test
    void resolveUrl_ActiveUrl_ReturnsOriginalUrl() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);
        String code = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        String result = service.resolveUrl(code);

        assertEquals("https://example.com", result);
    }

    @Test
    void resolveUrl_NonExistentCode_ReturnsNotFoundMessage() {
        String result = service.resolveUrl("NONEXIST");

        assertEquals("Ссылка не найдена", result);
    }

    @Test
    void updateUrlMaxClicks_ValidRequest_ReturnsTrue() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);
        String code = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        boolean result = service.updateUrlMaxClicks(testUserId, code, 20);

        assertTrue(result);
    }

    @Test
    void updateUrlMaxClicks_InvalidNewLimit_ThrowsException() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);
        String code = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        assertThrows(IllegalArgumentException.class, () -> {
            service.updateUrlMaxClicks(testUserId, code, 0);
        });
    }

    @Test
    void updateUrlMaxClicks_WrongUser_ReturnsFalse() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);
        String code = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        UUID otherUserId = UUID.randomUUID();
        boolean result = service.updateUrlMaxClicks(otherUserId, code, 20);

        assertFalse(result);
    }

    @Test
    void extendUrlLifetime_ValidRequest_ReturnsTrue() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);
        String code = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        boolean result = service.extendUrlLifetime(testUserId, code, 7);

        assertTrue(result);
    }

    @Test
    void extendUrlLifetime_InvalidDays_ThrowsException() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);
        String code = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        assertThrows(IllegalArgumentException.class, () -> {
            service.extendUrlLifetime(testUserId, code, 0);
        });
    }

    @Test
    void deleteUrl_ValidRequest_ReturnsTrue() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);
        String code = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        boolean result = service.deleteUrl(testUserId, code);

        assertTrue(result);

        String resolveResult = service.resolveUrl(code);
        assertEquals("Ссылка не найдена", resolveResult);
    }

    @Test
    void deleteUrl_WrongUser_ReturnsFalse() {
        String shortUrl = service.createShortUrl(testUserId, "https://example.com", 10);
        String code = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

        UUID otherUserId = UUID.randomUUID();
        boolean result = service.deleteUrl(otherUserId, code);

        assertFalse(result);
    }

    @Test
    void printUserUrls_UserWithLinks_PrintsCorrectly() {
        service.createShortUrl(testUserId, "https://example1.com", 5);
        service.createShortUrl(testUserId, "https://example2.com", 10);

        assertDoesNotThrow(() -> service.printUserUrls(testUserId));
    }

    @Test
    void printUserUrls_UserWithoutLinks_DoesNothing() {
        assertDoesNotThrow(() -> service.printUserUrls(testUserId));
    }

    @Test
    void printUserStats_UserWithLinks_PrintsStats() {
        service.createShortUrl(testUserId, "https://example1.com", 5);
        service.createShortUrl(testUserId, "https://example2.com", 10);

        String url1 = service.createShortUrl(testUserId, "https://example3.com", 5);
        String code1 = url1.substring(url1.lastIndexOf("/") + 1);
        service.resolveUrl(code1);
        service.resolveUrl(code1);
        service.resolveUrl(code1);

        assertDoesNotThrow(() -> service.printUserStats(testUserId));
    }

    @Test
    void printUserStats_UserWithoutLinks_PrintsNoLinksMessage() {
        assertDoesNotThrow(() -> service.printUserStats(testUserId));
    }
}