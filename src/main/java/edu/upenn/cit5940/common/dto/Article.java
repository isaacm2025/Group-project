package edu.upenn.cit5940.common.dto;

import java.time.LocalDate;

public class Article {

    private final String id;
    private final LocalDate date;
    private final String url;
    private final String title;
    private final String body;
    private final String source;
    private final String authors;

    public Article(
            String id,
            LocalDate date,
            String url,
            String title,
            String body,
            String source,
            String authors) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Article ID cannot be empty.");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Article title cannot be empty.");
        }

        this.id = id;
        this.date = date;
        this.url = url;
        this.title = title;
        this.body = body == null ? "" : body;
        this.source = source == null ? "" : source;
        this.authors = authors == null ? "" : authors;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getSource() {
        return source;
    }

    public String getAuthors() {
        return authors;
    }
}
