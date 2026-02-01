package com.shortUrlService.config;

import com.shortUrlService.domain.service.NotificationService;
import com.shortUrlService.infrastructure.notification.ConsoleNotificationService;
import com.shortUrlService.infrastructure.persistence.InMemoryUrlRepository;
import com.shortUrlService.infrastructure.shortening.ShortCodeGenerator;
import com.shortUrlService.application.UrlShrinkApplicationService;
import com.shortUrlService.domain.service.UrlShrinkDomainService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static String getBaseUrl() {
        String baseUrl = properties.getProperty("app.base-url");
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public static int getDefaultTtlDays() {
        return Integer.parseInt(properties.getProperty("app.default-ttl-days"));
    }

    public static int getDefaultMaxClicks() {
        return Integer.parseInt(properties.getProperty("app.default-max-clicks"));
    }

    public static int getShortCodeLength() {
        return Integer.parseInt(properties.getProperty("app.short-code-length"));
    }

    public static String getDomain() {
        return properties.getProperty("app.domain");
    }

    public static NotificationService createNotificationService() {
        return new ConsoleNotificationService();
    }

    public static UrlShrinkApplicationService createApplicationService() {
        var repository = new InMemoryUrlRepository();
        var codeGenerator = new ShortCodeGenerator();
        var notificationService = createNotificationService();
        var domainService = new UrlShrinkDomainService(repository, codeGenerator, notificationService);

        startCleanupThread(repository);

        return new UrlShrinkApplicationService(domainService);
    }

    private static void startCleanupThread(InMemoryUrlRepository repository) {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                    repository.deleteExpired();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
}