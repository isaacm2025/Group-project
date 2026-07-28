package edu.upenn.cit5940.datamanagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;

import edu.upenn.cit5940.common.dto.Article;

public class CsvArticleReader implements ArticleReader {

    @Override
    public List<Article> read(Path filePath) throws IOException {

        List<Article> articles = new ArrayList<>();

        try (CSVReaderHeaderAware reader =
                     new CSVReaderHeaderAware(
                             Files.newBufferedReader(filePath))) {

            Map<String, String> row;

            while ((row = reader.readMap()) != null) {
                try {
                    Article article = convertRow(row);
                    articles.add(article);

                } catch (IllegalArgumentException exception) {
                    // Add logger here in the next step.
                    System.out.println(
                            "Skipping malformed article: "
                                    + exception.getMessage());
                }
            }

        } catch (CsvValidationException exception) {
            throw new IOException(
                    "Unable to parse CSV file: "
                            + filePath,
                    exception);
        }

        return articles;
    }

    private Article convertRow(Map<String, String> row) {

        String id = row.get("uri");
        String title = row.get("title");
        String dateText = row.get("date");

        if (dateText == null || dateText.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing article date.");
        }

        LocalDate date;

        try {
            date = LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid article date: " + dateText);
        }

        return new Article(
                id,
                date,
                row.get("url"),
                title,
                row.get("body"),
                row.get("source"),
                row.get("authors"));
    }
}
