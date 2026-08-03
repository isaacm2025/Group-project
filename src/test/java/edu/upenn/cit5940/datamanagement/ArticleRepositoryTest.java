package edu.upenn.cit5940.datamanagement;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.common.dto.TopicCount;


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

        List<Article> results = repository.search("artificial intelligence");

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

         List<Article> results = repository.search("artificial nonexistent");

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

        assertTrue(repository.search("the").isEmpty());
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

    @Test
    void returnsTopTopicsForSelectedMonth() {

        Article first = new Article(
                "1",
                LocalDate.of(2024, 1, 5),
                "url1",
                "AI chips",
                "AI future",
                "Source",
                "Author");

        Article second = new Article(
                "2",
                LocalDate.of(2024, 1, 10),
                "url2",
                "AI robotics",
                "chips chips",
                "Source",
                "Author");

        Article outsideMonth = new Article(
                "3",
                LocalDate.of(2024, 2, 1),
                "url3",
                "AI",
                "AI AI AI",
                "Source",
                "Author");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(
                                first,
                                second,
                                outsideMonth),
                        Set.of("future"));

        List<TopicCount> results =
                repository.getTopTopics(
                        YearMonth.of(2024, 1),
                        2);

        assertEquals(2, results.size());

        assertEquals(
                "ai",
                results.get(0).getWord());

        assertEquals(
                3,
                results.get(0).getCount());

        assertEquals(
                "chips",
                results.get(1).getWord());

        assertEquals(
                3,
                results.get(1).getCount());
        }


   @Test
   void limitsTopTopicsToRequestedMaximum() {

        Article article = new Article(
                "1",
                LocalDate.of(2024, 1, 1),
                "url",
                "alpha bravo charlie delta echo foxtrot",
                "golf hotel india juliet kilo lima",
                "Source",
                "Author");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(article),
                        Set.of());

        List<TopicCount> results =
                repository.getTopTopics(
                        YearMonth.of(2024, 1),
                        10);

        assertEquals(10, results.size());
    }

   @Test
   void returnsInclusiveMonthlyTopicTrend() {

        Article january = new Article(
                "1",
                LocalDate.of(2024, 1, 1),
                "url1",
                "AI AI",
                "",
                "Source",
                "Author");

        Article march = new Article(
                "2",
                LocalDate.of(2024, 3, 1),
                "url2",
                "AI",
                "",
                "Source",
                "Author");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(january, march),
                        Set.of());

        Map<YearMonth, Integer> trend =
                repository.getTopicTrend(
                        "ai",
                        YearMonth.of(2024, 1),
                        YearMonth.of(2024, 3));

        assertEquals(3, trend.size());

        assertEquals(
                2,
                trend.get(YearMonth.of(2024, 1)));

        assertEquals(
                0,
                trend.get(YearMonth.of(2024, 2)));

        assertEquals(
                1,
                trend.get(YearMonth.of(2024, 3)));
   }

   @Test
   void rejectsReversedTrendPeriod() {

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(),
                        Set.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.getTopicTrend(
                        "ai",
                        YearMonth.of(2024, 3),
                        YearMonth.of(2024, 1)));
   }

   @Test
   void stopWordInQueryProducesNoAndMatches() {

        Article article =
                createArticle(
                        "1",
                        "Artificial Intelligence",
                        "New research");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(article),
                        Set.of("the"));

        assertTrue(
                repository.search(
                        "artificial the")
                        .isEmpty());
   }

   @Test
   void oneCharacterTermInQueryProducesNoMatches() {

        Article article =
                createArticle(
                        "1",
                        "Artificial Intelligence",
                        "New research");

        ArticleRepository repository =
                new ArticleRepository(
                        List.of(article),
                        Set.of());

        assertTrue(
                repository.search(
                        "artificial a")
                        .isEmpty());
   }

}
