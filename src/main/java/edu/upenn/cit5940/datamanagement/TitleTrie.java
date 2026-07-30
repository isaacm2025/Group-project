package edu.upenn.cit5940.datamanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TitleTrie {

    private static class TrieNode {

        /*
         * TreeMap keeps child characters alphabetically ordered.
         */
        private final Map<Character, TrieNode> children =
                new TreeMap<>();

        private boolean completeWord;
    }

    private final TrieNode root = new TrieNode();

    public void insert(String word) {

        String normalized = normalize(word);

        if (normalized.isEmpty()) {
            return;
        }

        TrieNode current = root;

        for (char character : normalized.toCharArray()) {
            current = current.children.computeIfAbsent(
                    character,
                    ignored -> new TrieNode());
        }

        current.completeWord = true;
    }

    public List<String> autocomplete(
            String prefix,
            int maximumSuggestions) {

        if (maximumSuggestions <= 0) {
            return List.of();
        }

        String normalizedPrefix = normalize(prefix);

        if (normalizedPrefix.isEmpty()) {
            return List.of();
        }

        TrieNode current = root;

        for (char character :
                normalizedPrefix.toCharArray()) {

            current = current.children.get(character);

            if (current == null) {
                return List.of();
            }
        }

        List<String> suggestions = new ArrayList<>();

        collectWords(
                current,
                new StringBuilder(normalizedPrefix),
                suggestions,
                maximumSuggestions);

        return suggestions;
    }

    private void collectWords(
            TrieNode node,
            StringBuilder currentWord,
            List<String> suggestions,
            int maximumSuggestions) {

        if (suggestions.size() >= maximumSuggestions) {
            return;
        }

        if (node.completeWord) {
            suggestions.add(currentWord.toString());

            if (suggestions.size() >= maximumSuggestions) {
                return;
            }
        }

        for (Map.Entry<Character, TrieNode> entry
                : node.children.entrySet()) {

            currentWord.append(entry.getKey());

            collectWords(
                    entry.getValue(),
                    currentWord,
                    suggestions,
                    maximumSuggestions);

            currentWord.deleteCharAt(
                    currentWord.length() - 1);

            if (suggestions.size() >= maximumSuggestions) {
                return;
            }
        }
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", "");
    }
}