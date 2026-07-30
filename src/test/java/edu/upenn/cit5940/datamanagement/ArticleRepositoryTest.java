package edu.upenn.cit5940.datamanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.upenn.cit5940.common.dto.Article;


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

    @Test
    void returnsArticlesInsideInclusiveDateRange() {

        Article first = new Article(
                "1",
                LocalDate.of(2024, 1, 1),
                "url1",
                "First Article",
                "Body",
                "Source",
                "Author");

        Article second = new Article(
                "2",
                LocalDate.of(2024, 1, 15),
                "url2",
                "Second Article",
                "Body",
                "Source",
                "Author");

        Article third = new Article(
                "3",
                LocalDate.of(2024, 2, 1),
                "url3",
                "Third Article",
                "Body",
                "Source",
                "Author");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(first, second, third),
                        Set.of());

        List<Article> results =
                repository.getArticlesByDateRange(
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 1, 31));

        assertEquals(2, results.size());
        assertEquals("1", results.get(0).getId());
        assertEquals("2", results.get(1).getId());
    }

    @Test
    void rejectsReversedDateRange() {

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(),
                        Set.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.getArticlesByDateRange(
                        LocalDate.of(2024, 2, 1),
                        LocalDate.of(2024, 1, 1)));
    }
}
