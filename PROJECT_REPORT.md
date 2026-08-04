Part 1:

This project is implemented in Java and organized as a Maven project. A Java Development Kit (JDK) version 25 or higher and Maven must be installed before compiling the project.

From the project root directory, run: mvn clean test

This command deletes old build files, compiles the source code, compiles the test code, and runs the unit tests.

To build the executable JAR file, run: mvn clean package

The Maven Shade Plugin packages the project together with its external dependencies, including Jackson and OpenCSV. After packaging, the executable JAR is created in the target directory: target/cit5940-project-skeleton-1.0-SNAPSHOT.jar


Execution:

The application accepts up to two optional command-line arguments:

The path to the article data file

The path to the log file

The data file can be either CSV or JSON. The program chooses the correct reader based on the file extension.

Run with default files

java -jar target/cit5940-project-skeleton-1.0-SNAPSHOT.jar

When no arguments are provided, the application uses:

Data file: data/articles.csv
Log file: tech_news_search.log

Run with a custom data file

java -jar target/cit5940-project-skeleton-1.0-SNAPSHOT.jar data/articles.json

In this case, the application loads data/articles.json and still uses the default log file, tech_news_search.log.

Run with a custom data file and log file

java -jar target/cit5940-project-skeleton-1.0-SNAPSHOT.jar data/articles.csv logs/my_run.log

The application creates the parent log directory when necessary. All application logs are written to the selected file instead of standard output.

The data/stop_words.txt file must also be present because it is used when building the search and topic indexes.


Application Modes:

After startup, the application displays a main menu with four options:

1.Interactive Mode

2.Command Mode

3.Help and Documentation

4.Exit

Interactive Mode guides the user through each feature with numbered menus and prompts. Command Mode accepts direct commands.

The main Command Mode commands are:

search <keyword(s)>
autocomplete <prefix>
topics <YYYY-MM>
trends <topic> <start> <end>
articles <start_date> <end_date>
article <id>
stats
help
menu

Dates use the YYYY-MM-DD format, and monthly periods use the YYYY-MM format.


Part 2: System Design:

System Architecture:

We implemented the application using an n-tier architecture. The main purpose of this structure was to keep user-interface code, application logic, and data-management code separate. This also made it easier for group members to work on different parts without putting all the logic in one class.

The overall dependency flow is:

Main
  -> Presentation Tier
  -> Application/Logic Tier
  -> Data Management Tier

Dependencies only flow downward. The UI does not access the repository directly, and the repository does not depend on UI classes.

Main / Dependency Initialization:

Main is the starting point of the program. It reads the optional command-line arguments, configures the logger, selects the correct article reader, loads the data, builds the repository, creates the service layer, and finally starts the user interface.

We used constructor-based dependency injection instead of creating dependencies inside the business-logic classes. For example, the repository is passed into NewsSearchService, and the service is passed into the UI classes.


Presentation Tier:

The presentation tier is in the edu.upenn.cit5940.ui package. Its main classes are:

1. ConsoleUI

2. CommandMode

3. InteractiveMode

This tier is responsible for displaying menus, reading input, validating basic command structure, and formatting results. It does not contain indexing or file-parsing logic.

ConsoleUI controls the main menu. CommandMode handles direct commands such as search, topics, and trends. InteractiveMode provides the same services through a guided numbered menu.


Application/Logic Tier:

The application tier is in the edu.upenn.cit5940.processor package. The main class is NewsSearchService.

This class acts as the connection between the UI and the repository. Its responsibilities include:

validating dates and monthly periods

logging user operations

calling repository methods

returning results in a form that the UI can display

preventing UI classes from depending directly on data-management classes

We also created InvalidInputException so recoverable input errors can be handled without crashing the application.


Data Management Tier:

The data-management tier is in the edu.upenn.cit5940.datamanagement package. It contains:

1. ArticleRepository

2. ArticleReader

3. CsvArticleReader

4. JsonArticleReader

5. TextNormalizer

6. TitleTrie

The two reader classes parse the input files and create Article objects. ArticleRepository stores articles and builds the in-memory indexes used by the different features. TextNormalizer applies the same tokenization rules across search, topics, and trends.


DTO / Model Package:

The shared model classes are in edu.upenn.cit5940.common.dto.

Article represents one article and stores fields such as its ID, date, URL, title, body, source, and authors. TopicCount stores a topic word and its frequency.

These classes are simple data containers that can be shared between tiers.


Logging Package:

AppLogger is in the edu.upenn.cit5940.logging package. It writes timestamped entries to the configured log file. We log application startup, data loading, malformed records, searches, topic queries, trend queries, errors, and application exit.



Data Structures and Refactoring


HashMap Inverted Index:

The inverted index was refactored from a Binary Search Tree implementation to a HashMap:

private final Map<String, Set<String>> invertedIndex;

Each normalized word maps to a set of article IDs containing that word. A HashMap was a better choice because average lookup time is O(1). This is important because every search keyword needs to be looked up quickly.

For a multi-keyword search, the application retrieves the article-ID set for each term and intersects the sets. This implements AND behavior, so an article is returned only when it contains every search term.

Stop words and one-character tokens are not added to the inverted index. If a user includes a term that does not exist in the index, the AND search returns no results.


Article ID HashMap:

We also store articles by their unique ID:

private final Map<String, Article> articlesById;

This supports the article <id> command. Average lookup time is O(1), which is much more efficient than scanning the full article list.


Trie for Autocomplete:

Autocomplete is implemented using TitleTrie. Words from article titles are inserted into the Trie.

A Trie is useful because searching for a prefix only requires following the characters in the prefix. After reaching the prefix node, the program performs a depth-first traversal to collect matching words. The number of suggestions is limited to 10.

The child nodes are stored in sorted order, so suggestions are returned alphabetically and consistently.


TreeMap for Date-Range Queries:

Articles are indexed by publication date using:

private final TreeMap<LocalDate, List<String>> articlesByDate;

A TreeMap keeps dates sorted. The date-range feature uses an inclusive sub-map from the starting date to the ending date. This allows the program to return articles in chronological order without sorting the entire dataset for every query.


Monthly Word-Frequency TreeMap:

For topics and trends, the repository stores precomputed monthly word counts:

private final TreeMap<YearMonth, Map<String, Integer>> monthlyWordCounts;

The outer TreeMap keeps months in chronological order. Each month maps to a HashMap containing word frequencies for that month.

During initialization, the title and body of each article are normalized. The resulting tokens are used to build both the inverted index and monthly frequency counts. Reusing the tokens avoids repeatedly scanning and normalizing the full dataset for every feature.


Heap for Top Topics:

The topics <YYYY-MM> command uses a PriorityQueue, which is Java's heap implementation.

Instead of sorting every word in a month, the program keeps a bounded heap containing the best 10 candidates. When a new candidate is better than the current lowest-ranked candidate, it replaces the root of the heap.

This approach is efficient because the heap never grows beyond 10 elements. After the heap is built, the final results are sorted by frequency in descending order, with alphabetical ordering used to break ties.


Topic Trends:

The trends command reads from the precomputed monthly word counts. The application loops from the starting YearMonth to the ending YearMonth, inclusive.

Each month is included in the output even when the topic count is zero. A LinkedHashMap is used for the final result so the insertion order, and therefore the chronological month order, is preserved when the service returns data to the UI.


Design Patterns:

Singleton Pattern: AppLogger

We used the Singleton pattern for the logger:

private static final AppLogger INSTANCE =
        new AppLogger();

private AppLogger() {
}

public static AppLogger getInstance() {
    return INSTANCE;
}

This pattern was appropriate because the whole application should write to one configured log file. If every class created a separate logger, the log path and output could become inconsistent. The Singleton gives all tiers access to the same logging instance.

The log path is configured once in Main, and the logger instance is passed to other components through their constructors.


Strategy Pattern: Article Readers:

We used the Strategy pattern for CSV and JSON parsing. Both readers implement the same interface:

public interface ArticleReader {
    List<Article> read(Path filePath)
            throws IOException;
}

The application selects the implementation based on the file extension:

if (fileName.endsWith(".csv")) {
    return new CsvArticleReader(logger);
}

if (fileName.endsWith(".json")) {
    return new JsonArticleReader(logger);
}

This was a good fit because CSV and JSON require different parsing logic, but the rest of the application only needs a list of Article objects. The repository and service layer do not need to know which file format was used.

This design also makes the program easier to extend. A future format could be supported by creating another ArticleReader implementation without changing the repository or UI.


Error Handling and Robustness:

The application was designed to continue running after recoverable errors.

Malformed article records are skipped instead of stopping the whole data-loading process. The program logs the record number and the reason it was skipped. In our dataset, five records had empty titles, and both the CSV and JSON readers successfully loaded 99,981 valid articles.

The program also handles:

missing or unreadable data files

unsupported file extensions

invalid menu choices

empty and non-numeric input

unknown commands

missing command arguments

invalid dates and monthly periods

reversed date or period ranges

article IDs that do not exist

searches with no results

For recoverable input errors, the application displays a useful message and returns to a usable prompt. Fatal startup errors display an explanation and exit gracefully.



Challenges Faced:

One of the biggest challenges was parsing the CSV file correctly. Some article bodies contain commas, quotation marks, and multiple lines. At first, reading the file as regular physical lines caused a very large number of malformed records and eventually an unterminated quoted-field error.

We fixed this by using OpenCSV's RFC-4180 parser and allowing multiline records. After this change, the CSV reader and JSON reader both loaded the same 99,981 valid articles.

Another challenge was handling malformed records without crashing. Some records had empty titles. We added validation in both readers, skipped those records, and logged a warning with the reason.

We also had to think about startup performance because the dataset contains almost 100,000 valid articles. Building search, autocomplete, date, topic, and trend structures takes more time than only parsing the file. To reduce unnecessary work, the normalized tokens from each article are reused when building the search index and monthly counts.

A separate challenge was packaging the project as a runnable JAR. A normal Maven JAR did not automatically include Jackson and OpenCSV. We added the Maven Shade Plugin so the final JAR contains all runtime dependencies and can be launched directly with java -jar.

Finally, working as a group caused some Git conflicts when two people changed the repository and service classes. We used separate branches, rebasing, and manual conflict review to avoid accidentally deleting another person's changes.



Testing:

We used JUnit tests for the main data structures and repository behavior. The tests cover:

text normalization

stop-word removal

strict AND search

article lookup

inclusive date ranges

invalid date ranges

Trie autocomplete

duplicate Trie entries

autocomplete result limits

monthly top topics

topic result limits

monthly trends

missing months with zero counts

invalid trend ranges

We also manually tested both Command Mode and Interactive Mode, including invalid commands and invalid date input. The final Maven test and package commands completed successfully, and the shaded JAR was tested with both CSV and JSON input files.