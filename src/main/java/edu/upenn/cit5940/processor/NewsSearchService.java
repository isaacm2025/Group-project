package edu.upenn.cit5940.processor;

import java.util.List;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.datamanagement.ArticleRepository;
import edu.upenn.cit5940.logging.AppLogger;

public class NewsSearchService {

    private final ArticleRepository repository;
    private final AppLogger logger;

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
            List<String> keywords) {

        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        logger.info(
                "Search query: "
                        + String.join(" ", keywords));

        return repository.search(keywords)
                .stream()
                .map(Article::getTitle)
                .toList();
    }

    public Article getArticleById(String id) {
        return repository.getArticleById(id);
    }

    public int getArticleCount() {
        return repository.getArticleCount();
    }
}
