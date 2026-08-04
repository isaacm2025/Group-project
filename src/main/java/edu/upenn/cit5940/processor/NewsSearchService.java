package edu.upenn.cit5940.processor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.common.dto.TopicCount;
import edu.upenn.cit5940.datamanagement.ArticleRepository;
import edu.upenn.cit5940.datamanagement.TextNormalizer;
import edu.upenn.cit5940.logging.AppLogger;

public class NewsSearchService {

    private final ArticleRepository repository;
    private final AppLogger logger;

    private static final String INVALID_DATE_MESSAGE =
            "Error: Invalid date provided. Please use the YYYY-MM-DD format "
                    + "with valid values.";

    private static final String INVALID_PERIOD_MESSAGE =
            "Error: Invalid period provided. Please use the YYYY-MM format "
                    + "with valid values.";

    private static final String INVALID_RANGE_MESSAGE =
            "Error: Invalid date range. The start value cannot be after "
                    + "the end value.";
    
    private static final String INVALID_TOPIC_MESSAGE =
        "Error: Invalid topic provided.";

    public NewsSearchService(
            ArticleRepository repository,
            AppLogger logger) {

        if (repository == null) {
            throw new IllegalArgumentException(
                    "Repository cannot be null.");
        }

        if (logger == null) {
            throw new IllegalArgumentException(
                    "Logger cannot be null.");
        }

        this.repository = repository;
        this.logger = logger;
    }

    public List<String> searchTitles(
            String query) {

        if (query == null || query.isEmpty()) {
            return List.of();
        }

        List<String> titles = repository.search(query)
                .stream()
                .map(Article::getTitle)
                .toList();

        logger.info(
                "Search query \"" + query + "\" returned " + titles.size() + " articles.");

        return titles;
    }

    public Article getArticleById(String id) {
        return repository.getArticleById(id);
    }

    public int getArticleCount() {
        return repository.getArticleCount();
    }

    public List<String> getArticleTitlesByDateRange(String start, String end) throws InvalidInputException {

        LocalDate startDate = parseDate(start);
        LocalDate endDate = parseDate(end);

        if (startDate.isAfter(endDate)) {
            throw new InvalidInputException(INVALID_RANGE_MESSAGE);
        }

        logger.info(
                "Article date-range query: "
                        + startDate
                        + " to "
                        + endDate);

        return repository
                .getArticlesByDateRange(
                        startDate,
                        endDate)
                .stream()
                .map(Article::getTitle)
                .toList();
    }

    public List<String> autocomplete(String prefix) {

        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }

        logger.info(
                "Autocomplete query: " + prefix);

        return repository.autocomplete(
                prefix,
                10);
    }

    public List<TopicCount> getTopTopics(
            String period)
            throws InvalidInputException {

        YearMonth parsedPeriod =
                parsePeriod(period);

        logger.info(
                "Topics query for period: "
                        + parsedPeriod);

        return repository.getTopTopics(
                parsedPeriod,
                10);
    }

    public Map<String, Integer> getTrends(
            String topic,
            String start,
            String end)
            throws InvalidInputException {

        List<String> topicTerms = TextNormalizer.tokenize(topic, Set.of());

        if (topicTerms.isEmpty()) {
            throw new InvalidInputException(INVALID_TOPIC_MESSAGE);
        }

        String normalizedTopic = topicTerms.get(0);

        YearMonth startPeriod =
                parsePeriod(start);

        YearMonth endPeriod =
                parsePeriod(end);

        if (startPeriod.isAfter(endPeriod)) {
            throw new InvalidInputException(
                    INVALID_RANGE_MESSAGE);
        }

        Map<YearMonth, Integer> repositoryResults =
                repository.getTopicTrend(
                        normalizedTopic,
                        startPeriod,
                        endPeriod);

        Map<String, Integer> results =
                new LinkedHashMap<>();

        for (Map.Entry<YearMonth, Integer> entry
                : repositoryResults.entrySet()) {

            results.put(
                    entry.getKey().toString(),
                    entry.getValue());
        }

        logger.info(
                "Trend query for topic "
                        + normalizedTopic
                        + " from "
                        + startPeriod
                        + " to "
                        + endPeriod);

        return results;
    }

    private LocalDate parseDate(String value) throws InvalidInputException {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new InvalidInputException(INVALID_DATE_MESSAGE);
        }
    }

    private YearMonth parsePeriod(String value) throws InvalidInputException {
        try {
            return YearMonth.parse(value.trim());
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new InvalidInputException(INVALID_PERIOD_MESSAGE);
        }
    }
}
