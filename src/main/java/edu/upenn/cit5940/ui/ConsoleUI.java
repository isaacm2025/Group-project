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

            if (!scanner.hasNext()) {
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
        System.out.println(
                "INTERACTIVE MODE: Guided interface.");
        System.out.println(
                "COMMAND MODE: Enter commands directly.");
        System.out.println(
                "AVAILABLE SERVICES include search articles "
                        + "and autocomplete.");
        System.out.println(
                "Press ENTER to return to the main menu.");

        if (scanner.hasNext()) {
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