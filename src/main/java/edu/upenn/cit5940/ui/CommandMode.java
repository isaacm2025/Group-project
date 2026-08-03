package edu.upenn.cit5940.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.common.dto.TopicCount;
import edu.upenn.cit5940.logging.AppLogger;
import edu.upenn.cit5940.processor.InvalidInputException;
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

            case "articles":
                executeArticlesByDate(parts);
                return false;

            case "autocomplete":
                executeAutocomplete(parts);
                return false;

            case "topics":
                executeTopics(parts);
                return false;

            case "trends":
                executeTrends(parts);
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

        String query = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

        List<String> titles =
                service.searchTitles(query);

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
        System.out.println(
                "articles <start_date> <end_date> "
                        + "- Browse articles by date");
        System.out.println(
                "autocomplete <prefix> "
                        + "- Get title-word suggestions");
    }


    private void executeArticlesByDate(
            String[] parts) {

        if (parts.length != 3) {
            System.out.println(
                    "Usage: articles "
                            + "<start_date> <end_date>");
            return;
        }

        try {
            List<String> titles = service.getArticleTitlesByDateRange(parts[1], parts[2]);

            if (titles.isEmpty()) {
                System.out.println("No articles found.");
                return;
            }

            System.out.println("Articles from " + parts[1] + " to " + parts[2] + ": ");

            for (String title : titles) {
                System.out.println(title);
            }

        } catch (InvalidInputException exception) {
            System.out.println(exception.getMessage());
            logger.warning("Invalid date range: " + parts[1] + " to " + parts[2]);
        }
    }

    private void executeAutocomplete(
            String[] parts) {

        if (parts.length != 2) {
            System.out.println(
                    "Usage: autocomplete <prefix>");
            return;
        }

        List<String> suggestions =
                service.autocomplete(parts[1]);

        if (suggestions.isEmpty()) {
            System.out.println(
                    "No autocomplete suggestions found.");
            return;
        }

        System.out.println(
                "Autocomplete suggestions:");

        for (String suggestion : suggestions) {
            System.out.println(suggestion);
        }
    }

    private void executeTopics(String[] parts) {

        if (parts.length != 2) {
            System.out.println("Usage: topics <YYYY-MM>");
            return;
        }

        try {
            List<TopicCount> topics = service.getTopTopics(parts[1]);

            if (topics.isEmpty()) {
                System.out.println("No topics found for " + parts[1] + ".");
                return;
            }

            System.out.println("Top topics for " + parts[1] + ":");

            for (TopicCount topic : topics) {
                System.out.println(topic.getWord() + ": " + topic.getCount());
            }

        } catch (InvalidInputException exception) {
            System.out.println(exception.getMessage());
            logger.warning("Invalid period: " + parts[1]);
        }
    }

    private void executeTrends(String[] parts) {

        if (parts.length != 4) {
            System.out.println("Usage: trends <topic> <start_period> <end_period>");
            return;
        }

        try {
            Map<String, Integer> trends =
                    service.getTrends(parts[1], parts[2], parts[3]);

            System.out.println("Trends for " + parts[1]
                    + " from " + parts[2] + " to " + parts[3] + ":");

            for (Map.Entry<String, Integer> entry : trends.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

        } catch (InvalidInputException exception) {
            System.out.println(exception.getMessage());
            logger.warning("Invalid trend request: " + String.join(" ", parts));
        }
    }
}