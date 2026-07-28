package edu.upenn.cit5940.datamanagement;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TextNormalizerTest {

    @Test
    void tokenizesAndRemovesStopWords() {

        Set<String> stopWords =
                Set.of("the", "and", "of");

        List<String> result =
                TextNormalizer.tokenize(
                        "The future of Quantum Chips and AI!",
                        stopWords);

        assertEquals(
                List.of(
                        "future",
                        "quantum",
                        "chips",
                        "ai"),
                result);
    }

    @Test
    void removesOneCharacterTokens() {

        List<String> result =
                TextNormalizer.tokenize(
                        "A I AI ML",
                        Set.of());

        assertEquals(
                List.of("ai", "ml"),
                result);
    }

    @Test
    void handlesNullAndEmptyText() {

        assertEquals(
                List.of(),
                TextNormalizer.tokenize(
                        null,
                        Set.of()));

        assertEquals(
                List.of(),
                TextNormalizer.tokenize(
                        "   ",
                        Set.of()));
    }
}

