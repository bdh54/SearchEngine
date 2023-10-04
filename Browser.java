package prog11;

import java.util.List;

public interface Browser {

  /** Load the page with this url.  Return false if it could not be
   * loaded. */
  boolean loadPage(String url);

  /** Return a list containing all the words on this page. */
  List<String> getWords();

  /** Return a list containing all the URLs on this page */
  List<String> getURLs();
}
