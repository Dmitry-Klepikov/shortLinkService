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
        return switch (command.toLowerCase()) {
            case "exit" -> true;
            case "help" -> {
                printHelp();
                yield false;
            }
            case "new" -> {
                currentUser = appService.registerUser();
                System.out.printf("Пользователь создан: %s%n", currentUser);
                yield false;
            }
            case "shorten" -> {
                handleShorten(scanner);
                yield false;
            }
            case "open" -> {
                handleOpen(scanner);
                yield false;
            }
            case "my" -> {
                handleMyLinks();
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
            - new           - зарегистрировать пользователя
            - shorten URL   - сократить ссылку
            - open CODE     - открыть короткую ссылку  
            - my            - мои ссылки
            - help          - справка
            - exit          - выход""");
    }

    private void handleShorten(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Сначала зарегистрируйтесь: 'new'");
            return;
        }

        System.out.print("Введите URL: ");
        String url = scanner.nextLine().trim();

        System.out.print("Макс. кликов [" + AppConfig.getDefaultMaxClicks() + "]: ");
        String clicksInput = scanner.nextLine().trim();
        int maxClicks = clicksInput.isEmpty() ?
                AppConfig.getDefaultMaxClicks() : Integer.parseInt(clicksInput);

        String shortUrl = appService.shortenUrl(currentUser, url, maxClicks);
        System.out.println("Создана: " + shortUrl);
    }

    private void handleOpen(Scanner scanner) {
        System.out.print("Код ссылки: ");
        String shortCode = scanner.nextLine().trim();
        String targetUrl = appService.openShortUrl(shortCode);
        System.out.println("Результат: " + targetUrl);

        if (targetUrl.startsWith("http")) {
            try {
                Desktop.getDesktop().browse(new URI(targetUrl));
                System.out.println("Открываю: " + targetUrl);
            } catch (Exception e) {
                System.out.println("Ошибка браузера: " + e.getMessage());
            }
        } else {
            System.out.println("URL: " + targetUrl);
        }
    }

    private void handleMyLinks() {
        if (currentUser == null) {
            System.out.println("Нет активного пользователя");
            return;
        }
        appService.listUserUrls(currentUser);
    }
}
