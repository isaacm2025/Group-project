package edu.upenn.cit5940.datamanagement;

import edu.upenn.cit5940.common.dto.Article;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArticleRepositoryTest {

    private Article createArticle(
            String id,
            String title,
            String body) {

        return new Article(
                id,
                LocalDate.of(2024, 1, 1),
                "https://example.com/" + id,
                title,
                body,
                "Test Source",
                "Test Author");
    }

    @Test
    void findsArticleById() {

        Article article =
                createArticle(
                        "1",
                        "Artificial Intelligence News",
                        "Technology update");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(article),
                        Set.of("the", "and"));

        assertEquals(
                article,
                repository.getArticleById("1"));
    }

    @Test
    void searchesUsingAndLogic() {

        Article first =
                createArticle(
                        "1",
                        "Artificial Intelligence",
                        "New research");

        Article second =
                createArticle(
                        "2",
                        "Artificial Biology",
                        "Medical research");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(first, second),
                        Set.of());

        List<Article> results =
                repository.search(
                        List.of(
                                "artificial",
                                "intelligence"));

        assertEquals(1, results.size());
        assertEquals("1", results.get(0).getId());
    }

    @Test
    void returnsEmptyWhenOneKeywordIsMissing() {

        Article article =
                createArticle(
                        "1",
                        "Artificial Intelligence",
                        "New research");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(article),
                        Set.of());

        List<Article> results =
                repository.search(
                        List.of(
                                "artificial",
                                "nonexistent"));

        assertTrue(results.isEmpty());
    }

    @Test
    void doesNotIndexStopWords() {

        Article article =
                createArticle(
                        "1",
                        "The Future",
                        "The technology industry");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(article),
                        Set.of("the"));

        assertTrue(
                repository.search(
                        List.of("the"))
                        .isEmpty());
    }
}
