package edu.upenn.cit5940.datamanagement;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.common.dto.TopicCount;

public class ArticleRepository {

    private final Map<String, Article> articlesById;
    private final Map<String, Set<String>> invertedIndex;
    private final Set<String> stopWords;
    private final TreeMap<LocalDate, List<String>> articlesByDate;
    private final TitleTrie titleTrie;
    private final TreeMap<YearMonth, Map<String, Integer>> monthlyWordCounts;
    

    public ArticleRepository(List<Article> articles, Set<String> stopWords) {

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
        this.monthlyWordCounts = new TreeMap<>();

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
            String searchableText =
                article.getTitle()
                        + " "
                        + article.getBody();

            List<String> tokens =
                    TextNormalizer.tokenize(
                            searchableText,
                            stopWords);

            indexArticle(article, tokens);
            indexMonthlyTopics(article, tokens);
        }
    }

    public List<String> autocomplete(
            String prefix,
            int maximumSuggestions) {

        return titleTrie.autocomplete(
                prefix,
                maximumSuggestions);
    }

    private void indexArticle(
            Article article,
            List<String> tokens) {

        for (String token : tokens) {
            invertedIndex
                    .computeIfAbsent(
                            token,
                            ignored -> new HashSet<>())
                    .add(article.getId());
        }
    }

    private void indexMonthlyTopics(
            Article article,
            List<String> tokens) {

        LocalDate date = article.getDate();

        if (date == null) {
            return;
        }

        YearMonth period =
                YearMonth.from(date);

        Map<String, Integer> counts =
                monthlyWordCounts.computeIfAbsent(
                        period,
                        ignored -> new HashMap<>());

        for (String token : tokens) {
            counts.merge(
                    token,
                    1,
                    Integer::sum);
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

        List<String> terms = TextNormalizer.tokenizeQuery(query);

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

    public List<TopicCount> getTopTopics(
            YearMonth period,
            int maximumResults) {

        if (period == null || maximumResults <= 0) {
            return List.of();
        }

        Map<String, Integer> counts =
                monthlyWordCounts.get(period);

        if (counts == null || counts.isEmpty()) {
            return List.of();
        }

        /*
        * The root is the worst item currently inside
        * the top-results heap:
        *
        * 1. Lower frequency is worse.
        * 2. For equal frequencies, a later
        *    alphabetical word is worse.
        */
        Comparator<TopicCount> worstFirst =
                (first, second) -> {

                    int countComparison =
                            Integer.compare(
                                    first.getCount(),
                                    second.getCount());

                    if (countComparison != 0) {
                        return countComparison;
                    }

                    return second.getWord()
                            .compareTo(first.getWord());
                };

        PriorityQueue<TopicCount> heap =
                new PriorityQueue<>(worstFirst);

        for (Map.Entry<String, Integer> entry
                : counts.entrySet()) {

            TopicCount candidate =
                    new TopicCount(
                            entry.getKey(),
                            entry.getValue());

            if (heap.size() < maximumResults) {
                heap.offer(candidate);
                continue;
            }

            if (worstFirst.compare(
                    candidate,
                    heap.peek()) > 0) {

                heap.poll();
                heap.offer(candidate);
            }
        }

        List<TopicCount> results =
                new ArrayList<>(heap);

        results.sort(
                Comparator
                        .comparingInt(
                                TopicCount::getCount)
                        .reversed()
                        .thenComparing(
                                TopicCount::getWord));

        return results;
    }

    public Map<YearMonth, Integer> getTopicTrend(
            String topic,
            YearMonth startPeriod,
            YearMonth endPeriod) {

        if (topic == null
                || startPeriod == null
                || endPeriod == null) {

            throw new IllegalArgumentException(
                    "Topic and periods cannot be null.");
        }

        if (startPeriod.isAfter(endPeriod)) {
            throw new IllegalArgumentException(
                    "Start period cannot be after end period.");
        }

        String normalizedTopic =
                TextNormalizer.normalizeTerm(topic);

        Map<YearMonth, Integer> results =
                new LinkedHashMap<>();

        YearMonth current = startPeriod;

        while (!current.isAfter(endPeriod)) {

            Map<String, Integer> counts =
                    monthlyWordCounts.get(current);

            int count =
                    counts == null
                            ? 0
                            : counts.getOrDefault(
                                    normalizedTopic,
                                    0);

            results.put(current, count);

            current = current.plusMonths(1);
        }

        return results;
    }
}