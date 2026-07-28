package edu.upenn.cit5940.datamanagement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.RFC4180ParserBuilder;
import com.opencsv.exceptions.CsvValidationException;

import edu.upenn.cit5940.common.dto.Article;

public class CsvArticleReader implements ArticleReader {

    private static final int MULTILINE_LIMIT = 1000;

    @Override
    public List<Article> read(Path filePath) throws IOException {

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "CSV file path cannot be null.");
        }

        if (!Files.exists(filePath)) {
            throw new IOException(
                    "CSV file does not exist: " + filePath);
        }

        if (!Files.isReadable(filePath)) {
            throw new IOException(
                    "CSV file is not readable: " + filePath);
        }

        List<Article> articles = new ArrayList<>();

        /*
         * RFC4180Parser supports quoted fields containing commas,
         * quotation marks, and embedded line breaks.
         */
        ICSVParser parser = new RFC4180ParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .build();

        try (CSVReader reader = new CSVReaderBuilder(
                Files.newBufferedReader(
                        filePath,
                        StandardCharsets.UTF_8))
                .withCSVParser(parser)
                .withMultilineLimit(MULTILINE_LIMIT)
                .build()) {

            String[] header = reader.readNext();

            if (header == null) {
                throw new IOException("CSV file is empty.");
            }

            Map<String, Integer> columnIndexes =
                    buildColumnIndexes(header);

            validateRequiredColumns(columnIndexes);

            String[] row;
            int recordNumber = 1;

            while ((row = reader.readNext()) != null) {
                recordNumber++;

                /*
                 * A bad individual record is recoverable.
                 * Skip it and continue reading the remaining records.
                 */
                if (row.length != header.length) {
                    System.out.printf(
                            "Skipping malformed record %d: "
                                    + "expected %d fields but found %d.%n",
                            recordNumber,
                            header.length,
                            row.length);
                    continue;
                }

                try {
                    Article article =
                            convertRow(row, columnIndexes);

                    articles.add(article);

                } catch (IllegalArgumentException exception) {
                    System.out.printf(
                            "Skipping malformed record %d: %s%n",
                            recordNumber,
                            exception.getMessage());
                }
            }

        } catch (CsvValidationException exception) {
            throw new IOException(
                    "Unable to parse CSV file "
                            + filePath
                            + ": "
                            + exception.getMessage(),
                    exception);
        }

        return articles;
    }

    /**
     * Creates a lookup from each normalized column name
     * to its position in the CSV record.
     */
    private Map<String, Integer> buildColumnIndexes(
            String[] header) {

        Map<String, Integer> indexes = new HashMap<>();

        for (int i = 0; i < header.length; i++) {
            String columnName = header[i];

            if (columnName == null) {
                continue;
            }

            /*
             * Remove a possible UTF-8 byte-order mark from
             * the first header value.
             */
            columnName = columnName
                    .replace("\uFEFF", "")
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (!columnName.isEmpty()) {
                indexes.put(columnName, i);
            }
        }

        return indexes;
    }

    /**
     * Confirms that the columns necessary to create an
     * Article are present in the CSV header.
     */
    private void validateRequiredColumns(
            Map<String, Integer> indexes)
            throws IOException {

        requireColumn(indexes, "uri");
        requireColumn(indexes, "date");
        requireColumn(indexes, "title");
    }

    private void requireColumn(
            Map<String, Integer> indexes,
            String columnName)
            throws IOException {

        if (!indexes.containsKey(columnName)) {
            throw new IOException(
                    "CSV is missing required column: "
                            + columnName);
        }
    }

    /**
     * Converts one valid CSV record into an Article.
     */
    private Article convertRow(
            String[] row,
            Map<String, Integer> indexes) {

        String id = getValue(row, indexes, "uri");
        String dateText = getValue(row, indexes, "date");
        String title = getValue(row, indexes, "title");

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "Article ID cannot be empty.");
        }

        if (title.isBlank()) {
            throw new IllegalArgumentException(
                    "Article title cannot be empty.");
        }

        if (dateText.isBlank()) {
            throw new IllegalArgumentException(
                    "Article date cannot be empty.");
        }

        LocalDate date;

        try {
            date = LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid article date: " + dateText);
        }

        /*
         * Make sure this argument order matches the constructor
         * in your Article.java file.
         */
        return new Article(
                id,
                date,
                getValue(row, indexes, "url"),
                title,
                getValue(row, indexes, "body"),
                getValue(row, indexes, "source"),
                getValue(row, indexes, "authors"));
    }

    /**
     * Safely obtains an optional column value.
     */
    private String getValue(
            String[] row,
            Map<String, Integer> indexes,
            String columnName) {

        Integer index = indexes.get(columnName);

        if (index == null
                || index < 0
                || index >= row.length
                || row[index] == null) {

            return "";
        }

        return row[index].trim();
    }
}
