package com.shortUrlService;

import com.shortUrlService.application.UrlShrinkApplicationService;
import com.shortUrlService.config.AppConfig;

import java.awt.Desktop;
import java.net.URI;
import java.util.Scanner;
import java.util.UUID;

public class UrlShrinkerApp {
    private final UrlShrinkApplicationService appService;
    private UUID currentUser;

    public UrlShrinkerApp() {
        this.appService = AppConfig.createApplicationService();
    }

    public static void main(String[] args) {
        new UrlShrinkerApp().run();
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        printWelcome();

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine().trim();

            if (handleCommand(command, scanner)) {
                break;
            }
        }
        scanner.close();
    }

    private boolean handleCommand(String command, Scanner scanner) {
        String[] parts = command.split("\\s+");
        String mainCommand = parts[0].toLowerCase();

        return switch (mainCommand) {
            case "exit" -> true;
            case "help" -> {
                printHelp();
                yield false;
            }
            case "new" -> {
                handleNewUser();
                yield false;
            }
            case "shorten" -> {
                handleShorten(parts, scanner);
                yield false;
            }
            case "open" -> {
                handleOpen(parts, scanner);
                yield false;
            }
            case "my" -> {
                handleMyLinks();
                yield false;
            }
            case "edit" -> {
                handleEdit(parts, scanner);
                yield false;
            }
            case "extend" -> {
                handleExtend(parts, scanner);
                yield false;
            }
            case "delete" -> {
                handleDelete(parts, scanner);
                yield false;
            }
            case "stats" -> {
                handleStats();
                yield false;
            }
            default -> {
                System.out.println("Неизвестная команда. 'help' для справки");
                yield false;
            }
        };
    }

    private void printWelcome() {
        System.out.println("""
            Консольный сервис сокращения ссылок
            Введите 'help' для начала работы""");
    }

    private void printHelp() {
        System.out.println("""
            Доступные команды:
            - new                     - зарегистрировать пользователя
            - shorten [URL]           - сократить ссылку
            - open [CODE]             - открыть короткую ссылку  
            - my                      - мои ссылки
            - edit CODE [LIMIT]       - изменить лимит кликов
            - extend CODE [DAYS]      - продлить срок жизни ссылки
            - delete CODE             - удалить ссылку
            - stats                   - статистика
            - help                    - справка
            - exit                    - выход""");
    }

    private void handleNewUser() {
        currentUser = appService.registerUser();
        System.out.printf("Пользователь создан: %s%n", currentUser);
        System.out.println("Сохраните этот ID для доступа к вашим ссылкам");
    }

    private void handleShorten(String[] parts, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Сначала зарегистрируйтесь: 'new'");
            return;
        }

        String url;
        if (parts.length > 1) {
            url = parts[1];
        } else {
            System.out.print("Введите URL: ");
            url = scanner.nextLine().trim();
        }

        System.out.print("Макс. кликов [" + AppConfig.getDefaultMaxClicks() + "]: ");
        String clicksInput = scanner.nextLine().trim();
        int maxClicks = clicksInput.isEmpty() ?
                AppConfig.getDefaultMaxClicks() : Integer.parseInt(clicksInput);

        try {
            String shortUrl = appService.shortenUrl(currentUser, url, maxClicks);
            System.out.println("Создана: " + shortUrl);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void handleOpen(String[] parts, Scanner scanner) {
        String shortCode;
        if (parts.length > 1) {
            shortCode = parts[1];
        } else {
            System.out.print("Код ссылки: ");
            shortCode = scanner.nextLine().trim();
        }

        String targetUrl = appService.openShortUrl(shortCode);
        System.out.println("Результат: " + targetUrl);

        if (targetUrl.startsWith("http")) {
            try {
                Desktop.getDesktop().browse(new URI(targetUrl));
                System.out.println("Открываю: " + targetUrl);
            } catch (Exception e) {
                System.out.println("Ошибка браузера: " + e.getMessage());
            }
        }
    }

    private void handleMyLinks() {
        if (currentUser == null) {
            System.out.println("Нет активного пользователя");
            return;
        }
        appService.listUserUrls(currentUser);
    }

    private void handleEdit(String[] parts, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Сначала зарегистрируйтесь: 'new'");
            return;
        }

        String shortCode;
        if (parts.length > 1) {
            shortCode = parts[1];
        } else {
            System.out.print("Код ссылки: ");
            shortCode = scanner.nextLine().trim();
        }

        int newMaxClicks;
        if (parts.length > 2) {
            newMaxClicks = Integer.parseInt(parts[2]);
        } else {
            System.out.print("Новый лимит кликов: ");
            newMaxClicks = Integer.parseInt(scanner.nextLine().trim());
        }

        boolean success = appService.updateUrlMaxClicks(currentUser, shortCode, newMaxClicks);
        if (success) {
            System.out.println("Лимит обновлен для ссылки: " + shortCode);
        } else {
            System.out.println("Не удалось обновить ссылку. Проверьте код и права доступа.");
        }
    }

    private void handleExtend(String[] parts, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Сначала зарегистрируйтесь: 'new'");
            return;
        }

        String shortCode;
        if (parts.length > 1) {
            shortCode = parts[1];
        } else {
            System.out.print("Код ссылки: ");
            shortCode = scanner.nextLine().trim();
        }

        int additionalDays;
        if (parts.length > 2) {
            additionalDays = Integer.parseInt(parts[2]);
        } else {
            System.out.print("Дополнительных дней: ");
            additionalDays = Integer.parseInt(scanner.nextLine().trim());
        }

        boolean success = appService.extendUrlLifetime(currentUser, shortCode, additionalDays);
        if (success) {
            System.out.println("Срок жизни ссылки продлен на " + additionalDays + " дней");
        } else {
            System.out.println("Не удалось продлить ссылку. Проверьте код и права доступа.");
        }
    }

    private void handleDelete(String[] parts, Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Сначала зарегистрируйтесь: 'new'");
            return;
        }

        String shortCode;
        if (parts.length > 1) {
            shortCode = parts[1];
        } else {
            System.out.print("Код ссылки для удаления: ");
            shortCode = scanner.nextLine().trim();
        }

        boolean success = appService.deleteUrl(currentUser, shortCode);
        if (success) {
            System.out.println("Ссылка удалена: " + shortCode);
        } else {
            System.out.println("Не удалось удалить ссылку. Проверьте код и права доступа.");
        }
    }

    private void handleStats() {
        if (currentUser == null) {
            System.out.println("Нет активного пользователя");
            return;
        }
        appService.printUserStats(currentUser);
    }
}