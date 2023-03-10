/** DJ **/
package org.recommender101.eval.metrics.extensions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.recommender101.data.Rating;
import org.recommender101.eval.interfaces.RecommendationlistEvaluator;
import org.recommender101.gui.annotations.R101Class;
import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;
import org.recommender101.tools.Debug;
import org.recommender101.tools.Utilities101;

/**
 * This method calculates the concentration of recommended items. Depending on the mode - either the distribution of
 * recommended items is calculated - or the distribution of recommended relevant items is calculated - or the Gini index
 * is calculated based on the popularity (no. of ratings) of the items
 * 
 * The recommended items are arranged in buckets according to their frequency of appearing in recommendation lists
 * 
 * @author DJ
 * 
 * 
 */
@R101Class(name="Gini", description="This method calculates the concentration of recommended items. Depending on the mode - either the distribution of recommended items is calculated - or the distribution of recommended relevant items is calculated - or the Gini index is calculated based on the popularity (no. of ratings) of the items")
public class Gini extends RecommendationlistEvaluator {

	/**
	 * Set this to true if the CSV files should contain percentages instead of absolute values
	 */
	private final boolean CSV_USE_PERCENTAGES_ON_Y_AXIS = true;

	/**
	 * The possible evaluation modes
	 */
	enum evalmode {
		recommended, bypopularity, byavgrating
	};

	/**
	 * What should be counted
	 */
	boolean onlyRelevant = false;

	/**
	 * The list length to be considered
	 */
	int topN = 10;

	/**
	 * How many items should we place in each bin
	 */
	int itemsPerBin = 20;

	/**
	 * Calculate the normalized Gini-Index between 0 and 1 (instead of 0 and 1-1/n)
	 */
	boolean normalized = false;
	
	/**
	 * The gini index
	 */
	float gini = -1;

	/**
	 * A map to count how often an item was (successfully) recommended
	 */
	Map<Integer, Integer> recommendationFrequencies = new HashMap<Integer, Integer>();

	/**
	 * Path to a directory where CSV files will be saved Static so that every instance writes to the same directory
	 */
	private String outputDir = null;

	/**
	 * HashMap which holds the output file paths for all instances of Gini (one file for each configuration)
	 */
	private static Map<String, String> paths = new HashMap<String, String>();

	/**
	 * Hashmap which holds String ArrayLists containing CSV file contents, one for each configuration
	 */
	private static Map<String, TreeMap<Integer, String>> fileContents = new TreeMap<String, TreeMap<Integer,String>>();

	/**
	 * ArrayList containing the file contents for this instance
	 */
	private Map<Integer, String> currFileContents = new TreeMap<Integer, String>();

	/**
	 * Default: Calculate the concentration of relevant recommended items
	 */
	evalmode mode = evalmode.recommended;

	/**
	 * We count each relevant item in the list. Either count only hits or not
	 * 
	 * @param user
	 *            the user - not relevant here
	 * @param list
	 *            the list of recommendations
	 */
	@Override
	public void addRecommendations(Integer user, List<Integer> list) {
		int cnt = 0;
		for (Integer item : list) {
			if (cnt >= topN) {
				break;
			}
			if (this.onlyRelevant) {
				if (isItemRelevant(user, item)) {
					cnt++;
					Utilities101.incrementMapValue(recommendationFrequencies, item);
				}
			} else {
				Utilities101.incrementMapValue(recommendationFrequencies, item);
				cnt++;
			}
		}
	}

	/**
	 * The method calculates the Gini index. It places the items in bins based on their popularity. The number of bins
	 * per items is a parameter (default 20)
	 */
	@Override
	public float getEvaluationResult() {

		// If we should write to a file, prepare the data structures and output file names
		if (outputDir != null) {
		
			if (paths.get(getConfigurationFileString()) == null) {
	
				// Path for this Gini configuration has not yet been set, setting it
				// now	
				if (!this.outputDir.endsWith(".txt")) {
					if (!this.outputDir.endsWith("/")) {
						this.outputDir += "/";
					}
					this.outputDir += "experiment_" + System.currentTimeMillis() + ".csv";
				}
				// Add to HashMap for later use
				paths.put(getConfigurationFileString(), this.outputDir);
			} else {
				this.outputDir = paths.get(getConfigurationFileString());
				// System.out.println("Using path: "+this.outputDir);
			}
			if (fileContents.get(getConfigurationFileString()) == null) {
				fileContents.put(getConfigurationFileString(), new TreeMap<Integer, String>());
			}
	
			this.currFileContents = fileContents.get(getConfigurationFileString());
		}

		// Add all the items which have never been successfully recommended (as
		// we need an equal basis for all)
		// System.out.println("How often has each item been recommended: " +
		// this.recommendationFrequencies);

		for (Integer item : testDataModel.getItems()) {
			if (this.recommendationFrequencies.get(item) == null) {
				this.recommendationFrequencies.put(item, 0);
			}
		}
		// Sort the frequency map in ascending order
		this.recommendationFrequencies = Utilities101.sortByValueDescending(this.recommendationFrequencies);
		// System.out.println("Sorted : " + this.recommendationFrequencies);

		// Mode 1) Calculate concentration of the recommended items
		// Organize the items in bins of the given size and sort them in
		// ascending order
		List<Integer> itemKeys = null;
		Map<Integer, Integer> nbRatingsPerItem = new HashMap<Integer, Integer>();
		if (mode == evalmode.recommended) {
			itemKeys = new ArrayList<Integer>(this.recommendationFrequencies.keySet());
		} else if (mode == evalmode.bypopularity) {
			// Initialize with the set of all items
			for (Integer item : getTrainingDataModel().getItems()) {
				nbRatingsPerItem.put(item, 0);
			}
			// Calculate the frequencies of items. Use the training data which
			// is more
			// informative about item popularity
			for (Rating r : getTrainingDataModel().getRatings()) {
				Utilities101.incrementMapValue(nbRatingsPerItem, r.item);
			}
			// Sort the things by popularity
			nbRatingsPerItem = Utilities101.sortByValueDescending(nbRatingsPerItem);

			itemKeys = new ArrayList<Integer>(nbRatingsPerItem.keySet());
		} else if (mode == evalmode.byavgrating) {
			Map<Integer, Float> avgRatingsPerItem = Utilities101.getItemAverageRatings(getTrainingDataModel().getRatings());
			avgRatingsPerItem = Utilities101.sortByValueDescending(avgRatingsPerItem);
			itemKeys = new ArrayList<Integer>(avgRatingsPerItem.keySet());
		}
		

		Collections.reverse(itemKeys);

		// Determine the bins
		// Place low-frequency items in the extra bin which is required
		// when the number of items per bin is not a factor of the number
		// of items
		int numberOfBins = recommendationFrequencies.size() / itemsPerBin;
		int correction = recommendationFrequencies.size() % itemsPerBin;
		long[] bins;
		if (correction == 0) {
			bins = new long[numberOfBins];
		} else {
			bins = new long[numberOfBins + 1];
		}

		// now fill them in de
		int currBin = 0;
		int currCount = 0;
		int totalCount = 0;

		int totalRecommendations = 0;
		// Fill data in ascending order
		for (Integer item : itemKeys) {
			// If we have placed the few first items in the non-full bin
			// go to the next bin
			totalCount++;
			bins[currBin] += this.recommendationFrequencies.get(item);
			totalRecommendations += this.recommendationFrequencies.get(item);
			currCount++;
			if (totalCount == correction) {
				// Jump to the next bin
				currBin++;
				currCount = 0;
			} else if (currCount >= itemsPerBin) {
				currBin++;
				currCount = 0;
			}
		}
		
		// Print out the values to a CSV file (timkraemer)
		if (this.outputDir != null) {
			// Create labels for x axis
			int looper = 0;
			if (correction != 0) {
				looper++;
			}

			// Write Gini configuration
			if (currFileContents.get(-1) == null) {
				currFileContents.put(-1, getConfigurationFileString());
			}

			// Write recommender name
			if (currFileContents.get(0) == null) {
				currFileContents.put(0, "");
			}

			currFileContents.put(0, currFileContents.get(0) + ";" + getRecommenderName());

			// Fill lines in HashMap
			for (int n = looper; n < bins.length; n++) {
				String output = "";

				if (CSV_USE_PERCENTAGES_ON_Y_AXIS) {
					output += roundDouble((double) (bins[n] * 100) / (double) totalRecommendations) + "%";
				} else {
					output += bins[n];
				}

				if (currFileContents.get(n + 1) == null) {
					currFileContents.put(n + 1, "Bin " + (n - looper) + ";" + output);
				} else {
					currFileContents.put(n + 1, currFileContents.get(n + 1) + ";" + output);
				}
			}

			// Write HashMap contents to file
			try {
				FileWriter fstream = new FileWriter(this.outputDir);
				BufferedWriter out = new BufferedWriter(fstream);

				for (Integer index : currFileContents.keySet()) {
					out.write(currFileContents.get(index) + "\r\n");
				}

				out.close();
			} catch (Exception e) {
				System.err.println("ERROR while writing to CSV file: " + e.getMessage());
			}
		}

		// Now sort the bins / not necessary for the frequency
		Arrays.sort(bins);

		// Calculate the Gini index for the bins, normalized between 0 and 1 or standard between 1 and 1-1/n
		if(normalized){
			gini = Utilities101.calculateNormalizedGini(bins);
		}
		else{
			gini = Utilities101.calculateGini(bins);
		}
		
		return gini;
	}



	/**
	 * A setter for the factory
	 * 
	 * @param n
	 */
	@R101Setting(displayName="Top N", minValue=0, type=SettingsType.INTEGER, defaultValue="10")
	public void setTopN(String n) {
		this.topN = Integer.parseInt(n);
	}

	/**
	 * Setter for the path to a directory where CSV files containing data for diagrams will be saved
	 * 
	 * @param path
	 */
	@R101Setting(displayName="Output dir", defaultValue="/",
			description="Directory for CSV output files", type=SettingsType.TEXT)
	public void setOutputDir(String path) {
		this.outputDir = path;
		Debug.log("Gini: Output directory set to: " + path);
	}

	/**
	 * A setter for object creation.
	 * 
	 * @param n
	 *            the number of items per bin
	 */
	@R101Setting(defaultValue="20", type=SettingsType.INTEGER,displayName="Items per bin",
			description="The number of items per bin", minValue=0)
	public void setItemsPerBin(String n) {
		this.itemsPerBin = Integer.parseInt(n);
	}

	/**
	 * Returns the current evaluation mode
	 * 
	 * @return
	 */
	public evalmode getMode() {
		return mode;
	}

	/**
	 * Sets the evaluation mode
	 * 
	 * @param mode
	 */
	@R101Setting(type=SettingsType.ARRAY, values={"recommended", "bypopularity"}, defaultValue="recommended",
			description="Sets the evaluation mode", displayName="Evaluation Mode")
	public void setMode(String themode) {
		this.mode = evalmode.valueOf(themode.toLowerCase());
	}

	/**
	 * A setter for object construction
	 * 
	 * @param val
	 */
	@R101Setting(type=SettingsType.BOOLEAN, displayName="Only relevant",
			defaultValue="false")
	public void setOnlyRelevant(String val) {
		this.onlyRelevant = Boolean.parseBoolean(val.toLowerCase());
	}
	
	/**
	 * A setter for object construction
	 * 
	 * @param val
	 */
	@R101Setting(type=SettingsType.BOOLEAN, defaultValue="false", displayName="Normalized")
	public void setNormalized(String val) {
		this.normalized = Boolean.parseBoolean(val.toLowerCase());
	}

	/**
	 * A private helper method which cuts off all decimal places off a double variable except the first two.
	 * 
	 * @param d
	 *            The double variable.
	 * @return The given variable???with all decimal places >2 cut off.
	 */
	private String roundDouble(double d) {
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(d);
	}
}
