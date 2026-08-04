package edu.upenn.cit5940.ui;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.common.dto.TopicCount;
import edu.upenn.cit5940.logging.AppLogger;
import edu.upenn.cit5940.processor.InvalidInputException;
import edu.upenn.cit5940.processor.NewsSearchService;


/**
 * Presentation-tier class for the guided menu.
 *
 * Prompts the user for each argument one field at a time, calls the
 * application tier, and renders the result. Contains no search logic and
 * no direct dependency on the data management tier.
 */

public class InteractiveMode {

    private static final String MENU_PROMPT = "Select a service (1-8): ";
    private static final String EMPTY_INPUT_MESSAGE = "Empty Input. Please enter a choice.";
    private static final String NON_NUMERIC_MESSAGE = "Please enter a valid number (1-8).";
    private static final String INVALID_CHOICE_MESSAGE = "Invalid choice. Please enter 1-8.";

    private final NewsSearchService service;
    private final AppLogger logger;
    private final Scanner scanner;

    public InteractiveMode(
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

    private void printMenu() {
        System.out.println("""
            ==================================================
                    INTERACTIVE MODE
            ==================================================
            This mode will guide you through each operation step by step.
            ----------------------------------------
                    AVAILABLE SERVICES
            ----------------------------------------
            1. Search Articles
            2. Get Autocomplete Suggestions
            3. View Top Topics
            4. Analyze Topic Trends
            5. Browse Articles by Date
            6. View Specific Article by ID
            7. Show Statistics
            8. Back to Main Menu
            ----------------------------------------""");
        System.out.print(MENU_PROMPT);
    }

    /**
     * Runs the guided menu until the user chooses to go back or input ends.
     */
    public void start() {
        logger.info("Starting interactive mode.");

        boolean running = true;

        while (running) {

            printMenu();
            String input = readLine();

            if (input == null) {
                return;
            }

            if (input.isEmpty()) {
                System.out.println(EMPTY_INPUT_MESSAGE);
                continue;
            }

            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(NON_NUMERIC_MESSAGE);
                continue;
            }

            running = handleChoice(choice);
        }
        logger.info("Finished interactive mode.");
    }

    private boolean handleChoice(int choice) {
        logger.info("Interactive Mode choice: " + choice);

        switch (choice) {
            case 1:
                doSearch();
                return true;
            case 2:
                doAutocomplete();
                return true;
            case 3:
                doTopTopics();
                return true;
            case 4:
                doTrends();
                return true;
            case 5:
                doBrowseByDate();
                return true;
            case 6:
                doViewArticle();
                return true;
            case 7:
                doStatistics();
                return true;
            case 8:
                System.out.println("Returning to the main menu.");
                return false;
            default:
                System.out.println(INVALID_CHOICE_MESSAGE);
                return true;
        }
    }

    private void doSearch() {

        String query =
                prompt("Enter search keyword(s): ");

        if (query == null || query.isEmpty()) {
            System.out.println(
                    "Please enter search keyword(s).");
            pause();
            return;
        }

        logger.info(
                "Interactive search query: "
                        + query);

        System.out.println("Search results:");

        printTitles(
                service.searchTitles(query));

        pause();
    }

    private void doAutocomplete() {

        String prefix = prompt("Enter a prefix: ");

        if (prefix == null || prefix.isEmpty()) {
            System.out.println("Enter a prefix: ");
            pause();
            return;
        }

        List<String> suggestions = service.autocomplete(prefix);

        if (suggestions.isEmpty()) {
            System.out.println("No autocomplete suggestions found.");
        } else {
            System.out.println("Autocomplete suggestions:");

            for (String suggestion : suggestions) {
                System.out.println(suggestion);
            }
        }
        pause();
    }

    private void doTopTopics() {

        String period = prompt("Enter a period (YYYY-MM): ");

        if (period == null || period.isEmpty()) {
            return;
        }

        try {
            List<TopicCount> topics = service.getTopTopics(period);

            if (topics.isEmpty()) {
                System.out.println("No such topic found.");
            } else {
                System.out.println("Top topics for " + period + ":");

                for (TopicCount topic : topics) {
                    System.out.println(
                            topic.getWord() + ": " + topic.getCount()
                    );
                }
            }
        } catch (InvalidInputException exception) {
            System.out.println(exception.getMessage());
            logger.warning("Invalid period in Interactive mode." + period);
        }
        pause();
    }

    private void doTrends() {

        String topic =
                prompt("Enter topic: ");

        String start =
                prompt(
                        "Enter start period "
                                + "(YYYY-MM): ");

        String end =
                prompt(
                        "Enter end period "
                                + "(YYYY-MM): ");

        try {
            Map<String, Integer> trends =
                    service.getTrends(
                            topic,
                            start,
                            end);

            if (trends.isEmpty()) {
                System.out.println(
                        "No trend data found.");

            } else {
                System.out.println(
                        "Trends for "
                                + topic
                                + " from "
                                + start
                                + " to "
                                + end
                                + ":");

                for (Map.Entry<String, Integer> entry
                        : trends.entrySet()) {

                    System.out.println(
                            entry.getKey()
                                    + ": "
                                    + entry.getValue());
                }
            }

        } catch (InvalidInputException exception) {
            System.out.println(
                    exception.getMessage());
        }

        pause();
    }

    private void doBrowseByDate() {
        String start = prompt("Enter start date (YYYY-MM-DD): ");

        if (start == null || start.isEmpty()) {
            return;
        }

        String end = prompt("Enter end date (YYYY-MM-DD): ");
        if (end == null || end.isEmpty()) {
            return;
        }

        try {
            printTitles(service.getArticleTitlesByDateRange(start, end));
        } catch (InvalidInputException exception) {
            System.out.println(exception.getMessage());
            logger.warning("Invalid date range in Interactive mode:" + start + " to " + end);
        }
        pause();
    }

    private void doViewArticle() {
        String id = prompt("Enter article id: ");
        if (id == null || id.isEmpty()) {
            System.out.println("Please enter an article id.");
            pause();
            return;
        }

        Article article = service.getArticleById(id);

        if (article == null) {
            System.out.println("Article with id " + id + " not found.");
        } else {
            printArticle(article);
        }
        pause();
    }

    private void doStatistics() {
        System.out.println("Total article count: " + service.getArticleCount());
        pause();
    }

    /**
     * @return the trimmed line, or null if the input stream has ended.
     */
    private String readLine() {

        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine().trim();
    }

    private String prompt(String message) {

        System.out.print(message);
        return readLine();
    }


    private void pause() {

        System.out.print("Press Enter to return to the menu...");

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    private void printTitles(List<String> titles) {

        if (titles == null || titles.isEmpty()) {
            System.out.println("No articles found.");
            return;
        }

        for (String title : titles) {
            System.out.println(title);
        }
    }

    private void printArticle(Article article) {

        System.out.println("Article Details");
        System.out.println("ID: " + article.getId());
        System.out.println("Date: " + article.getDate());
        System.out.println("Title: " + article.getTitle());
        System.out.println("Source: " + article.getSource());
        System.out.println("Authors: " + article.getAuthors());
        System.out.println("URL: " + article.getUrl());
        System.out.println("Body: " + article.getBody());
    }
    
}
