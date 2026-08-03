package edu.upenn.cit5940.datamanagement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class TextNormalizer {

    private TextNormalizer() {
        // Utility class: prevent construction.
    }

    /**
     * Loads stop words from a text file containing
     * one stop word per line.
     */
    public static Set<String> loadStopWords(
            Path stopWordsPath) throws IOException {

        if (stopWordsPath == null) {
            throw new IllegalArgumentException(
                    "Stop-words path cannot be null.");
        }

        if (!Files.exists(stopWordsPath)) {
            throw new IOException(
                    "Stop-words file does not exist: "
                            + stopWordsPath);
        }

        if (!Files.isReadable(stopWordsPath)) {
            throw new IOException(
                    "Stop-words file is not readable: "
                            + stopWordsPath);
        }

        Set<String> stopWords = new HashSet<>();

        for (String line : Files.readAllLines(
                stopWordsPath,
                StandardCharsets.UTF_8)) {

            String word = line
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (!word.isEmpty()) {
                stopWords.add(word);
            }
        }

        return Collections.unmodifiableSet(stopWords);
    }

    /**
     * Normalizes text into lowercase searchable tokens.
     *
     * Rules:
     * - convert to lowercase;
     * - replace punctuation with spaces;
     * - remove blank tokens;
     * - remove one-character tokens;
     * - remove stop words.
     */
    public static List<String> tokenize(
            String text,
            Set<String> stopWords) {

        if (text == null || text.isBlank()) {
            return List.of();
        }

        Set<String> safeStopWords =
                stopWords == null
                        ? Set.of()
                        : stopWords;

        String normalized = text
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim();

        if (normalized.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(
                        normalized.split("\\s+"))
                .filter(token -> !token.isBlank())
                .filter(token -> token.length() > 1)
                .filter(token ->
                        !safeStopWords.contains(token))
                .toList();
    }

    /**
     * Normalizes a single search term.
     */
    public static String normalizeTerm(String term) {

        if (term == null) {
            return "";
        }

        return term
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", "")
                .trim();
    }

    /**
     * Normalizes user search terms without removing
     * stop words or one-character terms.
     *
     * Those terms will simply have no inverted-index
     * entry, causing a strict AND search to return
     * no results.
     */
    public static List<String> tokenizeQuery(
            String query) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalized = query
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^\\p{L}\\p{N}]+",
                        " ")
                .trim();

        if (normalized.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(
                        normalized.split("\\s+"))
                .filter(token -> !token.isBlank())
                .toList();
    }
}
