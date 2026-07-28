package edu.upenn.cit5940.datamanagement;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cit5940.common.dto.Article;
import edu.upenn.cit5940.logging.AppLogger;

public class JsonArticleReader implements ArticleReader {

    private final AppLogger logger;
    private final ObjectMapper objectMapper;

    public JsonArticleReader(AppLogger logger) {

        if (logger == null) {
            throw new IllegalArgumentException(
                    "Logger cannot be null.");
        }

        this.logger = logger;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<Article> read(Path filePath)
            throws IOException {

        validateFile(filePath);

        List<Article> articles = new ArrayList<>();

        try (Reader fileReader = Files.newBufferedReader(
                    filePath,
                    StandardCharsets.UTF_8);

             JsonParser jsonParser =
                    objectMapper.getFactory()
                            .createParser(fileReader)) {

            JsonToken firstToken = jsonParser.nextToken();

            if (firstToken == null) {
                throw new IOException(
                        "JSON file is empty.");
            }

            if (firstToken != JsonToken.START_ARRAY) {
                throw new IOException(
                        "JSON file must contain an array "
                                + "of article objects.");
            }

            int recordNumber = 0;
            JsonToken token;

            while ((token = jsonParser.nextToken()) != null) {

                if (token == JsonToken.END_ARRAY) {
                    break;
                }

                recordNumber++;

                /*
                 * Every element in the top-level array
                 * should be a JSON object.
                 */
                if (token != JsonToken.START_OBJECT) {
                    logger.warning(
                            "Skipping malformed JSON record "
                                    + recordNumber
                                    + ": expected an object.");

                    jsonParser.skipChildren();
                    continue;
                }

                try {
                    JsonNode articleNode =
                            objectMapper.readTree(jsonParser);

                    Article article =
                            convertNode(articleNode);

                    articles.add(article);

                } catch (IllegalArgumentException exception) {
                    logger.warning(
                            "Skipping malformed JSON record "
                                    + recordNumber
                                    + ": "
                                    + exception.getMessage());
                }
            }

        } catch (JsonProcessingException exception) {
            throw new IOException(
                    "Unable to parse JSON file "
                            + filePath
                            + ": "
                            + exception.getOriginalMessage(),
                    exception);
        }

        return articles;
    }

    private void validateFile(Path filePath)
            throws IOException {

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "JSON file path cannot be null.");
        }

        if (!Files.exists(filePath)) {
            throw new IOException(
                    "JSON file does not exist: "
                            + filePath);
        }

        if (!Files.isRegularFile(filePath)) {
            throw new IOException(
                    "JSON path is not a regular file: "
                            + filePath);
        }

        if (!Files.isReadable(filePath)) {
            throw new IOException(
                    "JSON file is not readable: "
                            + filePath);
        }
    }

    private Article convertNode(JsonNode node) {

        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException(
                    "Article must be a JSON object.");
        }

        String id =
                getText(node, "uri").trim();

        String dateText =
                getText(node, "date").trim();

        String title =
                getText(node, "title").trim();

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
                    "Invalid article date: "
                            + dateText);
        }

        /*
         * Verify that this argument order matches
         * your Article constructor.
         */
        return new Article(
                id,
                date,
                getText(node, "url"),
                title,
                getText(node, "body"),
                getText(node, "source"),
                getText(node, "authors"));
    }

    private String getText(
            JsonNode node,
            String fieldName) {

        JsonNode valueNode = node.get(fieldName);

        if (valueNode == null || valueNode.isNull()) {
            return "";
        }

        return valueNode.asText("");
    }
}
