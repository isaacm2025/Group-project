package edu.upenn.cit5940.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.logging.AppLogger;
import edu.upenn.cit5940.processor.NewsSearchService;

public class CommandMode {

    private final NewsSearchService service;
    private final AppLogger logger;
    private final Scanner scanner;

    public CommandMode(
            NewsSearchService service,
            AppLogger logger,
            Scanner scanner) {

        if (service == null) {
            throw new IllegalArgumentException(
                    "Search service cannot be null.");
        }

        if (logger == null) {
            throw new IllegalArgumentException(
                    "Logger cannot be null.");
        }

        if (scanner == null) {
            throw new IllegalArgumentException(
                    "Scanner cannot be null.");
        }

        this.service = service;
        this.logger = logger;
        this.scanner = scanner;
    }

    public void start() {

        displayHeader();

        while (true) {
            System.out.print("> ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println(
                        "Please enter a command.");
                continue;
            }

            logger.info(
                    "User command: " + input);

            if (processCommand(input)) {
                return;
            }
        }
    }

    /**
     * @return true when Command Mode should end.
     */
    private boolean processCommand(String input) {

        String[] parts = input.split("\\s+");

        String command =
                parts[0].toLowerCase(Locale.ROOT);

        switch (command) {
            case "search":
                executeSearch(parts);
                return false;

            case "stats":
                displayStatistics();
                return false;

            case "help":
                displayHelp();
                return false;

            case "menu":
                System.out.println(
                        "Returning to the main menu.");
                return true;
            
            case "article":
                executeArticle(parts);
                return false;

            default:
                System.out.println(
                        "Unknown command. Type 'help' "
                                + "for available commands.");
                return false;
        }
    }

    private void executeSearch(String[] parts) {

        if (parts.length < 2) {
            System.out.println(
                    "Usage: search <keyword(s)>");
            return;
        }

        List<String> keywords =
                Arrays.asList(
                        Arrays.copyOfRange(
                                parts,
                                1,
                                parts.length));

        List<String> titles =
                service.searchTitles(keywords);

        if (titles.isEmpty()) {
            System.out.println(
                    "No articles found.");
            return;
        }

        System.out.println(
                "Search results:");

        for (String title : titles) {
            System.out.println(title);
        }

        System.out.println(
                titles.size() + " article(s) found.");
    }

    private void executeArticle(String[] parts) {

        if (parts.length != 2) {
            System.out.println(
                    "Usage: article <id>");
            return;
        }

        Article article =
                service.getArticleById(parts[1]);

        if (article == null) {
            System.out.println(
                    "No article found with ID: "
                            + parts[1]);
            return;
        }

        System.out.println("Article Details");
        System.out.println(
                "ID: " + article.getId());
        System.out.println(
                "Date: " + article.getDate());
        System.out.println(
                "Title: " + article.getTitle());
        System.out.println(
                "Source: " + article.getSource());
        System.out.println(
                "Authors: " + article.getAuthors());
        System.out.println(
                "URL: " + article.getUrl());
        System.out.println(
                "Body: " + article.getBody());
    }

    private void displayStatistics() {

        System.out.println(
                "Total articles: "
                        + service.getArticleCount());
    }

    private void displayHeader() {

        System.out.println(
                "==================================================");
        System.out.println("COMMAND MODE");
        System.out.println(
                "==================================================");
        System.out.println(
                "Enter commands directly. "
                        + "Type 'help' for available commands.");
        System.out.println(
                "Type 'menu' to return to the main menu.");
    }

    private void displayHelp() {

        System.out.println("AVAILABLE COMMANDS");
        System.out.println(
                "search <keyword(s)> - Search articles");
        System.out.println(
                "article <id>        - View article details");
        System.out.println(
                "stats               - Show statistics");
        System.out.println(
                "help                - Show available commands");
        System.out.println(
                "menu                - Return to the main menu");
    }
}