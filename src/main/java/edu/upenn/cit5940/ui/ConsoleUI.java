package edu.upenn.cit5940.ui;

import java.util.Scanner;

import edu.upenn.cit5940.logging.AppLogger;
import edu.upenn.cit5940.processor.NewsSearchService;

public class ConsoleUI {

    private final NewsSearchService service;
    private final AppLogger logger;
    private final Scanner scanner;
    private final CommandMode commandMode;
    private final InteractiveMode interactiveMode;

    public ConsoleUI(
            NewsSearchService service,
            AppLogger logger) {

        if (service == null) {
            throw new IllegalArgumentException(
                    "Search service cannot be null.");
        }

        if (logger == null) {
            throw new IllegalArgumentException(
                    "Logger cannot be null.");
        }

        this.service = service;
        this.logger = logger;
        this.scanner = new Scanner(System.in);
        this.commandMode = new CommandMode(
                service,
                logger,
                scanner);
        this.interactiveMode = new InteractiveMode(
                service,
                logger,
                scanner
        );
    }

    public void start() {

        boolean running = true;

        while (running) {

            displayMainMenu();

            if (!scanner.hasNextLine()) {
                running = false;
                break;
            }

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" ->
                        interactiveMode.start();

                case "2" ->
                        commandMode.start();

                case "3" ->
                        displayHelp();

                case "4" ->
                        running = false;

                case "" ->
                        System.out.println(
                                "Please enter a choice (1-4):");

                default ->
                        handleInvalidInput(input);
            }
        }

        logger.info("Application exiting");

        System.out.println(
                "Thank you for using the Tech News Search Engine!");
        System.out.println("Goodbye!");
    }

    private void displayMainMenu() {

        System.out.println(
                "==================================================");
        System.out.println("MAIN MENU");
        System.out.println(
                "==================================================");
        System.out.println(
                "1. Interactive Mode (Guided Menu)");
        System.out.println(
                "2. Command Mode (Direct Commands)");
        System.out.println(
                "3. Help & Documentation");
        System.out.println("4. Exit");
        System.out.println(
                "==================================================");
        System.out.print(
                "Please select an option (1-4): ");
    }

    private void displayHelp() {

        System.out.println(
                "==================================================");
        System.out.println(
                "HELP & DOCUMENTATION");
        System.out.println(
                "==================================================");

        System.out.println("INTERACTIVE MODE:");
        System.out.println(
                "Guided step-by-step interface.");

        System.out.println("COMMAND MODE:");
        System.out.println(
                "Enter commands directly.");

        System.out.println("AVAILABLE SERVICES:");
        System.out.println("1. Search Articles");
        System.out.println("2. Autocomplete");
        System.out.println("3. Top Topics");
        System.out.println("4. Topic Trends");
        System.out.println("5. Browse Articles");
        System.out.println("6. View Article");
        System.out.println("7. Statistics");

        System.out.println("DATE FORMATS:");
        System.out.println(
                "Period: YYYY-MM");
        System.out.println(
                "Date: YYYY-MM-DD");

        System.out.println(
                "Press ENTER to return "
                        + "to the main menu.");

        if (scanner.hasNextLine()) {
                scanner.nextLine();
        }
   }

    private void handleInvalidInput(String input) {

        try {
            Integer.parseInt(input);

            System.out.println(
                    "Invalid choice. Please enter 1-4.");

        } catch (NumberFormatException exception) {
            System.out.println(
                    "Please enter a valid number (1-4).");
        }
    }
}