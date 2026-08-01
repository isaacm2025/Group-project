package edu.upenn.cit5940.common.dto;

/**
 * Immutable pairing of a word with how often it appeared in a period.
 * Used to rank trending topics.
 */

public class TopicCount implements Comparable<TopicCount> {
    private final String word;
    private final int count;

    public TopicCount(String word, int count) {

        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException(
                    "Word cannot be null or empty.");
        }

        if (count < 0) {
            throw new IllegalArgumentException(
                    "Count cannot be negative.");
        }

        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    /**
     * Orders by count ascending, so the smallest count sits at the root
     * of a min heap and is the first candidate for eviction.
     * Ties break alphabetically for deterministic output.
     */
    @Override
    public int compareTo(TopicCount other) {

        int byCount = Integer.compare(this.count, other.count);

        if (byCount != 0) {
            return byCount;
        }

        return this.word.compareTo(other.word);
    }

    @Override
    public String toString() {
        return word + ": " + count;
    }
}
