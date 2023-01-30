package org.recommender101.tools;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.recommender101.Recommender101;
import org.recommender101.data.DataModel;
import org.recommender101.data.Rating;
import org.recommender101.eval.impl.Recommender101Impl;

/**
 * A class to collect statistics about data sets
 * @author Dietmar
 *
 */
public class DataSetStatistics {

	/**
	 * Main entry point (no parameters) 
	 */
	public static void main(String[] args) {
		System.out.println("Starting data set stats");
		try {
			DataSetStatistics stats = new DataSetStatistics();
			stats.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Stats collection ended");
	}
	
	
	/**
	 * The main worker method for the tests
	 * @throws Exception
	 */
	public void run() throws Exception {
		Properties props = new Properties();
		props.load(new FileReader("conf/recommender101.properties"));
		Recommender101 r101 = new Recommender101(props);
		collectStatistics(r101.getDataModel());
	}

	
	/**
	 * Collects and prints the various statistics  
	 * @param dataModel
	 * @throws Exception
	 */
	public void collectStatistics(DataModel dataModel) throws Exception {
		printBasicStatistics(dataModel);
	}
	
	
	/**
	 * Prints the basic statistics such as #users, #items, #ratings, sparsity level
	 * @param dataModel
	 * @throws Exception
	 */
	public void printBasicStatistics(DataModel dataModel) throws Exception {
		System.out.println("Basic data set statistics ");
		System.out.println("-----------------------------");
		System.out.println("#Users: \t\t" + dataModel.getUsers().size());
		System.out.println("#Items: \t\t" + dataModel.getItems().size());
		System.out.println("#Ratings: \t\t" + dataModel.getRatings().size());
		System.out.println("Sparsity: \t\t" + Recommender101Impl.decimalFormat.format((double) dataModel.getRatings().size() / ((double) (dataModel.getItems().size() * dataModel.getUsers().size()))));
		System.out.println("-----------------------------");
		

		if(!dataModel.getRatings().isEmpty()){
			Frequency freq = new Frequency();
			DescriptiveStatistics stats = new DescriptiveStatistics();
			for (Rating rating : dataModel.getRatings()) {
				stats.addValue(rating.rating);
				freq.addValue(rating.rating);
			}
			System.out.println("Global avg: \t\t" + Recommender101Impl.decimalFormat.format(stats.getMean()));
			System.out.println("Global median: \t\t" + Recommender101Impl.decimalFormat.format(stats.getPercentile(50)));
			System.out.println("Standard deviation: \t" + Recommender101Impl.decimalFormat.format(stats.getStandardDeviation()));
			System.out.println("-----------------------------");
			System.out.println("Ratings freqs:");
			System.out.print(freq);
			System.out.println("-----------------------------");
			//Skewness: a measure of how skewed a distribution is, relative to a normal distribution - that is, how asymmetric it is. Positive values indicate a distribution with a tail inclining to the positive side, and negative values a distribution with a tail inclining to the negative side
			System.out.println("Skewness: \t\t" + Recommender101Impl.decimalFormat.format(stats.getSkewness()));
			//Kurtosis: a measure of how peaked or flat a distribution is, relative to a normal distribution. Positive values indicate a relatively peaked distribution, and negative a relatively flat distribution.
			System.out.println("Kurtosis: \t\t" + Recommender101Impl.decimalFormat.format(stats.getKurtosis()));
		}
		
		
		// Rating statistics
		Map<Float, Integer> frequencies = Utilities101.getRatingFrequencies(dataModel);
		// Get the Gini index of the frequencies
		Map<Float, Integer> sortedFreqs = Utilities101.sortByValueDescending(frequencies);
		//System.out.println("Sorted: " + sortedFreqs);
		List<Float> levels = new ArrayList<>();
		for (Float key: sortedFreqs.keySet()) {
			levels.add(0,key);
		}
		//System.out.println("levels: " + levels);
		long[] bins = new long[levels.size()];
		for (int i = 0; i < levels.size(); i++){
			bins[i] = frequencies.get(levels.get(i));
		}
		double gini = Utilities101.calculateGini(bins);
		System.out.println("Gini of freqs: \t\t" + gini);

		// Avg ratings per user and item
		System.out.println("Avg. Ratings/user: \t" + Recommender101Impl.decimalFormat.format((double) dataModel.getRatings().size() / dataModel.getUsers().size()));
		System.out.println("Avg. Ratings/item: \t" + Recommender101Impl.decimalFormat.format((double)dataModel.getRatings().size() / dataModel.getItems().size()));
		System.out.println("Min. Ratings/user: \t" + dataModel.getMinUserRatings());
		System.out.println("Min. Ratings/item: \t" + dataModel.getMinItemRatings());
		System.out.println("Max. Ratings/user: \t" + dataModel.getMaxUserRatings());
		System.out.println("Max. Ratings/item: \t" + dataModel.getMaxItemRatings());
		System.out.println("-----------------------------");
	}
	
 	
}
