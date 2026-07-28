package edu.upenn.cit5940;

import java.nio.file.Path;
import java.util.List;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.datamanagement.ArticleReader;
import edu.upenn.cit5940.datamanagement.CsvArticleReader;

public class Main {

    public static void main(String[] args) {

        String dataFile =
                args.length >= 1
                        ? args[0]
                        : "data/articles.csv";

        System.out.println(
                "=== Tech News Search Engine ===");
        System.out.println(
                "Initializing n-tier architecture...");
        System.out.println(
                "Loading articles from: " + dataFile);

        try {
            ArticleReader reader =
                    new CsvArticleReader();

            List<Article> articles =
                    reader.read(Path.of(dataFile));

            if (articles.isEmpty()) {
                System.out.println(
                        "No valid articles were loaded.");
                return;
            }

            System.out.println(
                    articles.size() + " articles loaded");

            System.out.println(
                    "Architecture initialization complete!");

        } catch (Exception exception) {
            System.out.println(
                    "Unable to start application: "
                            + exception.getMessage());
        }
    }
}
