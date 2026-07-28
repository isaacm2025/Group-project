package edu.upenn.cit5940;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.datamanagement.ArticleReader;
import edu.upenn.cit5940.datamanagement.CsvArticleReader;
import edu.upenn.cit5940.datamanagement.JsonArticleReader;
import edu.upenn.cit5940.datamanagement.TextNormalizer;
import edu.upenn.cit5940.logging.AppLogger;

public class Main {

    private static ArticleReader createReader(
        Path dataPath,
        AppLogger logger) {

        String fileName = dataPath
                .getFileName()
                .toString()
                .toLowerCase();

        if (fileName.endsWith(".csv")) {
            return new CsvArticleReader(logger);
        }

        if (fileName.endsWith(".json")) {
            return new JsonArticleReader(logger);
        }

        throw new IllegalArgumentException(
                "Unsupported data-file format: " + fileName);
    }

    private static final String DEFAULT_DATA_FILE =
            "data/articles.csv";

    private static final String DEFAULT_LOG_FILE =
            "tech_news_search.log";

    public static void main(String[] args) {

        String dataFile =
                args.length >= 1
                        ? args[0]
                        : DEFAULT_DATA_FILE;

        String logFile =
                args.length >= 2
                        ? args[1]
                        : DEFAULT_LOG_FILE;

        Path dataPath = Path.of(dataFile);
        Path logPath = Path.of(logFile);

        AppLogger logger = AppLogger.getInstance();

        System.out.println(
                "=== Tech News Search Engine ===");
        System.out.println(
                "Initializing n-tier architecture...");
        System.out.println(
                "Loading articles from: " + dataFile);

        try {
            /*
             * Configure logging before creating the other
             * application components.
             */
            logger.configure(logPath);

            logger.info("Application starting");
            logger.info(
                    "Loading articles from: " + dataFile);

            /*
             * CSV is the only implemented reader at this stage.
             * Later, select CSV or JSON based on the extension.
             */
            ArticleReader reader = createReader(dataPath, logger);

            List<Article> articles =
                    reader.read(dataPath);

            /*
             * Zero valid articles is a fatal startup error.
             */
            if (articles.isEmpty()) {
                throw new IllegalStateException(
                        "The data file contains zero valid articles.");
            }

            Set<String> stopWords =
                    TextNormalizer.loadStopWords(
                            Path.of("data/stop_words.txt"));

            logger.info(
                    "Loaded "
                            + stopWords.size()
                            + " stop words");

            logger.info(
                    "Loaded "
                            + articles.size()
                            + " valid articles");

            System.out.println(
                    articles.size() + " articles loaded");

            System.out.println(
                    "Architecture initialization complete!");
            
            logger.info(
                "Architecture initialization complete");

            /*
             * Later, construct these components here:
             *
             * ArticleRepository repository =
             *         new ArticleRepository(articles, ...);
             *
             * NewsSearchService service =
             *         new NewsSearchService(repository);
             *
             * ConsoleUI ui =
             *         new ConsoleUI(service, ...);
             *
             * ui.start();
             */

        } catch (Exception exception) {

            logger.error(
                    "Application startup failed: "
                            + exception.getMessage());

            System.out.println(
                    "Unable to start application: "
                            + exception.getMessage());

        } finally {
            /*
             * This is appropriate while the program ends
             * immediately after loading. Later, it will close
             * only after the UI exits.
             */
            logger.close();
        }
    }
}
