package prog11;

import java.util.List;

public interface SearchEngine {
    /* Collect info from all web pages reachable from URLs in startingURLs. */
    void collect (Browser browser, List<String> startingURLs);

    /* Assign a priority to each web page using the PageRank algorithm. */
    void rank (boolean fast);

    /* Search for up to numResults pages containing all searchWords and
     * return them in an array in order of decreasing importance
     * (number of references). */
    String[] search (List<String> searchWords, int numResults);
}

