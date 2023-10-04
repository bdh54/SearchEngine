package prog11;

import prog02.GUI;
import java.util.*;

//import javax.xml.stream.events.StartDocument;

public class Rank {
    public static void main(String[] args) {
	 // String pageDiskName = "pagedisk-mary.txt";
	 // String wordDiskName = "worddisk-mary.txt";
	String pageDiskName = "pagedisk-1.txt";
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

	notGPT.rank(false);
	System.out.println("page disk after slow rank");
	for (InfoFile file : g.pageDisk.values())
	    System.out.println(file);
	
	g.pageDisk.write("slow.txt");

	notGPT.rank(true);
	System.out.println("page disk after fast rank");
	for (InfoFile file : g.pageDisk.values())
	    System.out.println(file);

	g.pageDisk.write("fast.txt");
    }
}
