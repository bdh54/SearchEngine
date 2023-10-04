package prog11;

import prog02.GUI;
import java.util.*;

import javax.xml.stream.events.StartDocument;

public class Search {
    public static void main(String[] args) {
	//String pageDiskName = "pagedisk-mary-ranked.txt";
	//String wordDiskName = "worddisk-mary.txt";
	 String pageDiskName = "pagedisk-1-ranked.txt";
	 String wordDiskName = "worddisk-1.txt";

	Browser browser = new BetterBrowser();
	SearchEngine notGPT = new NotGPT();

	NotGPT g = (NotGPT) notGPT;

	g.pageDisk.read(pageDiskName);
	for (Map.Entry<Long,InfoFile> entry : g.pageDisk.entrySet())
	    g.url2index.put(entry.getValue().data, entry.getKey());

	g.wordDisk.read(wordDiskName);
	for (Map.Entry<Long,InfoFile> entry : g.wordDisk.entrySet())
	    g.word2index.put(entry.getValue().data, entry.getKey());

	System.out.println("map from URL to page index");
	System.out.println(g.url2index);
	System.out.println("map from page index to page disk");
	System.out.println(g.pageDisk);
	System.out.println("map from word to word index");
	System.out.println(g.word2index);
	System.out.println("map from word index to word file");
	System.out.println(g.wordDisk);

	List<String> keyWords = new ArrayList<String>();
	if (false) {
	    keyWords.add("mary");
	    keyWords.add("jack");
	    keyWords.add("jill");
	} else {
	    GUI gui = new GUI("NotGPT");
	    while (true) {
		String input = gui.getInfo("Enter search words.");
		if (input == null)
		    return;
		String[] words = input.split("\\s", 0);
		keyWords.clear();
		for (String word : words)
		    keyWords.add(word);
		String[] urls = notGPT.search(keyWords, 5);
		String res = "Found " + keyWords + " on";
		for (int i = 0; i < urls.length; i++)
		    res = res + "\n" + BetterBrowser.inverseReversePathURL(urls[i]);
		gui.sendMessage(res);
	    }
	}

	String[] urls = notGPT.search(keyWords, 5);

	System.out.println("Found " + keyWords + " on");
	for (int i = 0; i < urls.length; i++)
	    System.out.println(BetterBrowser.inverseReversePathURL(urls[i]));
    }
}
