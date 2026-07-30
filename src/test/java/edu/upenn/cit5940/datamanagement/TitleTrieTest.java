package edu.upenn.cit5940.datamanagement;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TitleTrieTest {

    @Test
    void returnsWordsStartingWithPrefixAlphabetically() {

        TitleTrie trie = new TitleTrie();

        trie.insert("artificial");
        trie.insert("article");
        trie.insert("artist");
        trie.insert("apple");

        List<String> suggestions =
                trie.autocomplete("arti", 10);

        assertEquals(
                List.of(
                        "article",
                        "artificial",
                        "artist"),
                suggestions);
    }

    @Test
    void limitsNumberOfSuggestions() {

        TitleTrie trie = new TitleTrie();

        for (int index = 0; index < 20; index++) {
            trie.insert("technology" + index);
        }

        List<String> suggestions =
                trie.autocomplete("tech", 10);

        assertEquals(10, suggestions.size());
    }

    @Test
    void duplicateWordsAreReturnedOnce() {

        TitleTrie trie = new TitleTrie();

        trie.insert("artificial");
        trie.insert("artificial");
        trie.insert("artificial");

        assertEquals(
                List.of("artificial"),
                trie.autocomplete("art", 10));
    }

    @Test
    void returnsEmptyForUnknownPrefix() {

        TitleTrie trie = new TitleTrie();

        trie.insert("artificial");

        assertTrue(
                trie.autocomplete(
                        "xyz",
                        10)
                        .isEmpty());
    }

    @Test
    void searchIsCaseInsensitive() {

        TitleTrie trie = new TitleTrie();

        trie.insert("Artificial");

        assertEquals(
                List.of("artificial"),
                trie.autocomplete("ART", 10));
    }
}