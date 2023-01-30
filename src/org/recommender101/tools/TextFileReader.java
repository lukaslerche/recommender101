package org.recommender101.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Dietmar
 * From: http://www.java2s.com/Code/Java/Language-Basics/Javaforinforinlinebylineiterationthroughatextfile.htm
 */
/**
 * This class allows line-by-line iteration through a text file. The iterator's
 * remove() method throws UnsupportedOperatorException. The iterator wraps and
 * rethrows IOExceptions as IllegalArgumentExceptions.
 */
public class TextFileReader implements Iterable<String> {

	// Used by the TextFileIterator class below
	final String filename;

	// progress display
	boolean displayProgress = false;

	// how many lines
	int lineCount = -1;
	// ten percent
	int tenPerCent = -1;

	// the counter
	int cnt = -1;
	
	// max lines for debugging
	public int maxLines = -1; 

	public TextFileReader(String filename) {
		this.filename = filename;
	}

	/**
	 * With an additional parameter
	 * 
	 * @param filename
	 * @param progress
	 */
	public TextFileReader(String filename, boolean progress) {
		this(filename);
		this.displayProgress = progress;
		if (this.displayProgress == true) {
			lineCount = getNumberOfLines(filename);
			// progress parameters
			tenPerCent = Math.max(1, lineCount / 10);
		}
	}

	// This is the one method of the Iterable interface
	public Iterator<String> iterator() {
		return new TextFileIterator(this);
	}

	/**
	 * A method to load a list of String from a text file.
	 * @param input the name of the text file
	 * @return the list of String
	 */
	public static List<String> loadList(String input, boolean progress){
		List<String> list = new ArrayList<String>();
		TextFileReader tfr = new TextFileReader(input, progress);
		for (String line: tfr) list.add(line);
		return list;
	}

	// This non-static member class is the iterator implementation
	class TextFileIterator implements Iterator<String> {

		// the creating class
		TextFileReader textfile;
		
		// The stream we're reading from
		BufferedReader in;

		// Return value of next call to next()
		String nextline;

		public TextFileIterator(TextFileReader tf) {
			this.textfile = tf;
			// Open the file and read and remember the first line.
			// We peek ahead like this for the benefit of hasNext().
			try {
				in = new BufferedReader(new FileReader(filename));
				nextline = in.readLine();
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		// If the next line is non-null, then we have a next line
		public boolean hasNext() {
//			System.out.println(maxLines + " " + textfile.cnt);
			if (maxLines > 0 && textfile.cnt > maxLines) {
				return false;
			}
			return nextline != null;
		}

		// Return the next line, but first read the line that follows it.
		public String next() {
			try {
				if (textfile.displayProgress && ++textfile.cnt % textfile.tenPerCent == 0) {
					System.out.println(Math.round(100 * (double) textfile.cnt / textfile.lineCount) + " %");
				}
				

				String result = nextline;

				// If we haven't reached EOF yet
				if (nextline != null) {
					nextline = in.readLine(); // Read another line
					if (nextline == null)
						in.close(); // And close on EOF
				}

				// Return the line we read last time through.
				return result;
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		// The file is read-only; we don't allow lines to be removed.
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * A method to count the number of lines of a text file
	 * @param filename
	 * @return
	 */
	public static int getNumberOfLines(String filename) {
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr, 1024 * 1024 * 50);
			int lineNumber = 0;
			while(br.readLine() != null) lineNumber++;
			fr.close();
			br.close();
			return lineNumber;
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return -1;
	}

	public static String readFirstline(String filename) {
		try {
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			fr.close();
			br.close();
			return line;
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
}
