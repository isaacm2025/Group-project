package edu.upenn.cit5940.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AppLogger {

    private static final AppLogger INSTANCE =
            new AppLogger();

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss");

    private BufferedWriter writer;

    private AppLogger() {
    }

    public static AppLogger getInstance() {
        return INSTANCE;
    }

    public void configure(Path logPath)
            throws IOException {

        if (logPath == null) {
            throw new IllegalArgumentException(
                    "Log path cannot be null.");
        }

        Path parent = logPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        if (writer != null) {
            writer.close();
        }

        writer = Files.newBufferedWriter(
                logPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    public void info(String message) {
        write("INFO", message);
    }

    public void warning(String message) {
        write("WARNING", message);
    }

    public void error(String message) {
        write("ERROR", message);
    }

    private synchronized void write(
            String level,
            String message) {

        if (writer == null) {
            return;
        }

        try {
            writer.write(String.format(
                    "[%s] %-7s %s%n",
                    LocalDateTime.now()
                            .format(FORMATTER),
                    level,
                    message));

            writer.flush();

        } catch (IOException exception) {
            // Avoid recursively attempting to log
            // a logger failure.
        }
    }

    public void close() {
        if (writer == null) {
            return;
        }

        try {
            writer.close();
        } catch (IOException exception) {
            // Nothing else can be done safely here.
        }
    }
}