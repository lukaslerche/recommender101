package org.recommender101.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * A class to write Strings in text files.
 * 
 * <p>Not sure it is relevant. Just wanted to avoid having a simple TextReader on one side
 * and a BufferedWriter with surrounding try-catches on the other.
 */
public class TextFileWriter {
	
	private BufferedWriter out;
	
	private boolean somethingNew = false;

	public TextFileWriter(String filename, boolean append) {
		try {
			out = new BufferedWriter(new FileWriter(filename, append));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public TextFileWriter(String filename) {
		this(filename, false);
	}

	/**
	 * A method to write a new line in the text file.
	 * @param line
	 */
	public void writeNextLine(String line) {
		try {
			out.write(line);
			out.newLine();
			somethingNew = true;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public void close(){
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A method to append a String at the end of the file. The String can have several lines and the method does not add a new line.
	 * @param lines
	 */
	public void append(String lines) {
		try {
			out.write(lines);
			somethingNew = true;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

    /**
     * Flushes the stream if something new was written since the last time the stream was flushed.
     */
	public void flush() {
		if (somethingNew){
			try {
				out.flush();
				somethingNew = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * A method to save some String content into a file.
	 * @param content the content to save
	 * @param filename the name of the file
	 */
	public static void save(String content, String filename) {
		TextFileWriter tfw = new TextFileWriter(filename, false);
		tfw.append(content);
		tfw.close();
	}

	/**
	 * A method to save a list of String in a file
	 * @param list the list of String
	 * @param file the file name
	 */
	public static void save(List<String> list, String filename) {
		TextFileWriter tfw = new TextFileWriter(filename);
		for (String line: list) tfw.writeNextLine(line);
		tfw.close();
	}
}
