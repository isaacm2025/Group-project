package edu.upenn.cit5940.datamanagement;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.upenn.cit5940.common.dto.Article;

public class ArticleRepository {

    private final Map<String, Article> articlesById;
    private final Map<String, Set<String>> invertedIndex;
    private final Set<String> stopWords;
    private final TreeMap<LocalDate, List<String>> articlesByDate;
    private final TitleTrie titleTrie;

    public ArticleRepository(
            List<Article> articles,
            Set<String> stopWords) {

        if (articles == null) {
            throw new IllegalArgumentException(
                    "Articles cannot be null.");
        }

        this.articlesById = new HashMap<>();
        this.invertedIndex = new HashMap<>();
        this.stopWords =
                stopWords == null
                        ? Set.of()
                        : new HashSet<>(stopWords);
        this.articlesByDate = new TreeMap<>();
        this.titleTrie = new TitleTrie();

        buildIndexes(articles);
    }

    private void buildIndexes(List<Article> articles) {

        for (Article article : articles) {
            if (article == null) {
                continue;
            }

            articlesById.put(
                    article.getId(),
                    article);

            indexTitleWords(article);

            LocalDate date = article.getDate();

            if (date != null) {
                articlesByDate.computeIfAbsent(article.getDate(), ignored -> new ArrayList<>()).add(article.getId());

            }
            indexArticle(article);
        }
    }

    public List<String> autocomplete(
            String prefix,
            int maximumSuggestions) {

        return titleTrie.autocomplete(
                prefix,
                maximumSuggestions);
    }

    private void indexArticle(Article article) {

        String searchableText =
                article.getTitle()
                        + " "
                        + article.getBody();

        List<String> tokens =
                TextNormalizer.tokenize(
                        searchableText,
                        stopWords);

        for (String token : tokens) {
            invertedIndex
                    .computeIfAbsent(
                            token,
                            ignored -> new HashSet<>())
                    .add(article.getId());
        }
    }

    public Article getArticleById(String id) {

        if (id == null) {
            return null;
        }

        return articlesById.get(id);
    }

    public int getArticleCount() {
        return articlesById.size();
    }

    public int getIndexedWordCount() {
        return invertedIndex.size();
    }

    public Set<String> getArticleIdsForWord(
            String word) {

        String normalized =
                TextNormalizer.normalizeTerm(word);

        Set<String> ids =
                invertedIndex.get(normalized);

        if (ids == null) {
            return Set.of();
        }

        return Collections.unmodifiableSet(ids);
    }

    public List<Article> search(
            String query) {

        List<String> terms = TextNormalizer.tokenize(query, stopWords);

        if (terms.isEmpty()) {
            return List.of();
        }

        Set<String> matchingIds = null;

        for (String term : terms) {

            Set<String> ids = invertedIndex.get(term);

            if (ids == null || ids.isEmpty()) {
                return List.of();
            }

            if (matchingIds == null) {
                matchingIds = new HashSet<>(ids);
            } else {
                matchingIds.retainAll(ids);
            }

            if (matchingIds.isEmpty()) {
                return List.of();
            }
        }

        if (matchingIds == null) {
            return List.of();
        }

        List<Article> results = new ArrayList<>();

        for (String id : matchingIds) {
            Article article =
                    articlesById.get(id);

            if (article != null) {
                results.add(article);
            }
        }

        results.sort(
                (first, second) ->
                        first.getTitle()
                                .compareToIgnoreCase(
                                        second.getTitle()));

        return results;

    }


    public List<Article> getArticlesByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date cannot be null.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date.");
        }

        List<Article> results = new ArrayList<>();

        Map<LocalDate, List<String>> range =
                articlesByDate.subMap(
                        startDate,
                        true,
                        endDate,
                        true);

        for (Map.Entry<LocalDate, List<String>> entry
                : range.entrySet()) {

            for (String articleId : entry.getValue()) {
                Article article =
                        articlesById.get(articleId);

                if (article != null) {
                    results.add(article);
                }
            }
        }

        return results;
    }

    private void indexTitleWords(Article article) {

        /*
        * Use an empty stop-word set because autocomplete
        * is based on words appearing in titles, not only
        * words included in the inverted search index.
        */
        List<String> titleWords =
                TextNormalizer.tokenize(
                        article.getTitle(),
                        Set.of());

        for (String word : titleWords) {
            titleTrie.insert(word);
        }
    }

}