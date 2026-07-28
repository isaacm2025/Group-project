package edu.upenn.cit5940.datamanagement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import edu.upenn.cit5940.common.dto.Article;

public interface ArticleReader {

    List<Article> read(Path filePath) throws IOException;
}