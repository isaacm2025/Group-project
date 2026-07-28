package edu.upenn.cit5940.datamanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.upenn.cit5940.common.dto.Article;

public class ArticleRepository {

    private final Map<String, Article> articlesById;
    private final Map<String, Set<String>> invertedIndex;
    private final Set<String> stopWords;

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

            indexArticle(article);
        }
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
            List<String> keywords) {

        if (keywords == null
                || keywords.isEmpty()) {
            return List.of();
        }

        Set<String> matchingIds = null;

        for (String keyword : keywords) {
            String normalized =
                    TextNormalizer.normalizeTerm(keyword);

            if (normalized.isBlank()) {
                return List.of();
            }

            Set<String> ids =
                    invertedIndex.get(normalized);

            /*
             * The search uses AND logic.
             * If one keyword is absent, there are no matches.
             */
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
}
