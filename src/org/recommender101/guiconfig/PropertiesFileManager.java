package org.recommender101.guiconfig;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.recommender101.tools.Debug;

public class PropertiesFileManager {

	public static void main(String[] args) {
		//InternalR101PropertiesFile f = getPropertiesFile("conf/recommender101.properties");
		InternalR101PropertiesFile f = getPropertiesFile("C:/basic.properties");
		System.out.println(f.toString());
	}

	public static void savePropertiesFile(InternalR101PropertiesFile props, String path)
	{
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		
		Debug.log("Saving properties file to: "+path);
		
		try{
			FileWriter fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(props.toString());
			//Close the output stream
			out.close();
		}
		catch (Exception e){
			  System.err.println("Error while saving properties: " + e.getMessage());
		}
		
	}
	
	
	public static InternalR101PropertiesFile getPropertiesFile(String path) {

		// Create file if it doesn't exist yet
		File f = new File(path);
		if (!f.exists()) {
			Debug.log("[GUI] Properties file not found, creating file: " + path);
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BufferedInputStream stream = null;

		try {
			stream = getInputStream(path);
		} catch (FileNotFoundException e1) {
			// File not found, although it should have been created
			e1.printStackTrace();
		}

		Properties properties = new Properties();

		if (stream != null) {
			try {
				properties.load(stream);
				stream.close();
			} catch (IOException e) {
				// Other IO Exception
				e.printStackTrace();
			}
		}

		// Get comments etc.
		String fileContent = "";
		try {
			fileContent = new java.util.Scanner(new File(path)).useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		HashMap<String, ArrayList<String>> comments = parseCommentsAndEmptyLines(fileContent);

		return getInternalPropFile(properties, comments);
	}

	public static InternalR101PropertiesFile parsePropertiesFile(String contents) {

		Properties properties = new Properties();
		try {
			properties.load(new StringReader(contents));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getInternalPropFile(properties, parseCommentsAndEmptyLines(contents));
	}

	private static HashMap<String, ArrayList<String>> parseCommentsAndEmptyLines(String r) {
		String[] arr = r.split("\r\n");

		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		
		ArrayList<String> currComments = new ArrayList<String>();
		boolean inProperty = false;
		for (String line : arr) {
			String originalLine = line;
			line = line.trim();
			//System.out.println(inProperty +"+++"+line);
			if (inProperty) {
				// Do nothing, just check if the next line will still be within
				// this property
				inProperty = line.endsWith("\\");
			} else {
				if (line.contains("=") && (!line.startsWith("#")) && (!line.startsWith("!"))) {

					// The following if makes sure that no lines with classnames and settings (which may contain a =) are wrongly
					// considered as a property
					if (!(line.contains(":") && line.indexOf(":") < line.indexOf("=")))
					{
						// This line contains a property
						inProperty = line.endsWith("\\");
						
						// Associate the above comments with this property
						map.put(line.substring(0, line.indexOf("=")).trim(),currComments);
						currComments = new ArrayList<String>();
					}
					else {
						currComments.add("#" + originalLine);
					}
					
				} else {
					if (line.equals("") || line.startsWith("#") || line.startsWith("!")) {
						// The empty line shall be remembered
						// But 1 empty line maximum to keep the file clean
						if (currComments.size() == 0 || (!line.equals("")))
						{
							currComments.add(line);
						}
						else if (!currComments.get(currComments.size()-1).equals(""))
						{
							//currComments.add(line);
							currComments.add("");
						}
					} else {
						currComments.add("#" + originalLine);
					}
				}
			}
		}

		//System.out.println("---");
		/*for (String key:map.keySet())
		{
			System.out.println("Comments for: "+key);
			for (String s:map.get(key))
			{
				System.out.println(s);
			}
		}*/
		
		return map;
	}

	private static InternalR101PropertiesFile getInternalPropFile(Properties properties, HashMap<String, ArrayList<String>> comments) {

		InternalR101PropertiesFile propFile = new InternalR101PropertiesFile();

		// Recommenders
		String recString = properties.getProperty("AlgorithmClasses");
		if (recString != null) {
			String[] recStringArray = recString.split(",");
			for (String s : recStringArray) {
				InternalR101Class c = new InternalR101Class(s);
				propFile.getRecommenders().add(c);
				if(comments.get("AlgorithmClasses") != null)
				{
					propFile.setRecommendersComments(comments.get("AlgorithmClasses"));
				}
			}
		}

		// Metrics
		String metString = properties.getProperty("Metrics");
		if (metString != null) {
			String[] metStringArray = metString.split(",");
			for (String s : metStringArray) {
				InternalR101Class c = new InternalR101Class(s);
				propFile.getMetrics().add(c);
				if(comments.get("Metrics") != null)
				{
					propFile.setMetricsComments(comments.get("Metrics"));
				}
			}
		}

		// Other settings
		for (Object key : properties.keySet()) {
			String currKey = (String) key;

			if (currKey.equals("AlgorithmClasses") || currKey.equals("Metrics")) {
				continue;
			}
			if (comments.get(currKey) != null)
			{
				//System.out.println("adding "+currKey+" with "+comments.get(currKey).size()+" comments");			
				propFile.addOtherSetting(currKey, properties.getProperty(currKey), comments.get(currKey));
			}
			else
			{
				//System.out.println("null for "+currKey);
			}
		}

		Debug.log("[GUI] Properties file loaded.");
		return propFile;
	}

	private static BufferedInputStream getInputStream(String path) throws FileNotFoundException {
		return new BufferedInputStream(new FileInputStream(path));
	}

}
