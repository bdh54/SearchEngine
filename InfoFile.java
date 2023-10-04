package prog11;

import java.util.List;
import java.util.ArrayList;

/** This class represents the information stored in a file to record a
 * word or web page. */
public class InfoFile {
    public final String data; // URL or word
    public double priority = 0;
    public double tempPriority;
    List<Long> indices = new ArrayList<Long>(); // page indices

    public InfoFile (String data) {
	this.data = data;
    }

    public String toString () {
	return data + indices + priority;
    }
}


    
