/** DJ **/
package org.recommender101.tools;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.recommender101.data.DataModel;
import org.recommender101.data.Rating;
import org.recommender101.eval.impl.Recommender101Impl;

/**
 * Various helpers
 * 
 * @author DJ
 * 
 */
public class Utilities101 {

	/**
	 * Calculate the number of ratings per item
	 * 
	 * @param dm
	 * @return the map with the rating counts
	 */
	public static Map<Integer, Integer> calculateRatingsPerItem(DataModel dm) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (Rating r : dm.getRatings()) {
			Utilities101.incrementMapValue(result, r.item);
		}
		return result;
	}

	// =====================================================================================

	/**
	 * Sort a Map by value in descending order
	 ** 
	 * @param map
	 * @return a sorted map
	 */
	public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValueDescending(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sort a Map by value in ascending order
	 ** 
	 * @param map
	 * @return a sorted map
	 */
	public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValueAscending(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sort a Map by key in descending order
	 ** 
	 * @param map
	 * @return a sorted map
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sortByKeyDescending(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getKey()).compareTo(o1.getKey());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sort a Map by key in ascending order
	 ** 
	 * @param map
	 * @return a sorted map
	 */
	public static <K extends Comparable<? super K>, V> Map<K, V> sortByKeyAscending(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getKey()).compareTo(o2.getKey());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Returns true if a rating exists in the collection of ratings
	 * @param user
	 * @param item
	 * @param ratings
	 * @return true, if the rating exists (only comparing user and item id)
	 */
	public static boolean ratingExists(int user, int item, Set<Rating> ratings) {
		for (Rating r : ratings) {
			if (r.user == user && r.item == item) {
				return true;
			}
		}
		return false;
	} 
	

	// =====================================================================================

	/**
	 * Returns a map with user averages given a set of ratings
	 */
	public static Map<Integer, Float> getUserAverageRatings(Set<Rating> ratings) {
		Map<Integer, Float> result = new HashMap<Integer, Float>();
		Map<Integer, Integer> counters = new HashMap<Integer, Integer>();

		for (Rating r : ratings) {
			Float userAvg = result.get(r.user);
			if (userAvg == null) {
				userAvg = new Float(r.rating);
				result.put(r.user, (float) r.rating);
				counters.put(r.user, 1);
			} else {
				counters.put(r.user, counters.get(r.user) + 1);
				result.put(r.user, result.get(r.user) + r.rating);
			}
		}
		// Divide by number of ratings
		for (Integer user : result.keySet()) {
			result.put(user, result.get(user) / (float) counters.get(user));
		}
		return result;
	}

	// =====================================================================================

	/**
	 * Returns a map with user averages given a set of ratings
	 */
	public static Map<Integer, Float> getItemAverageRatings(Set<Rating> ratings) {
		Map<Integer, Float> result = new HashMap<Integer, Float>();
		Map<Integer, Integer> counters = new HashMap<Integer, Integer>();

		for (Rating r : ratings) {
			Float itemAvg = result.get(r.item);
			if (itemAvg == null) {
				itemAvg = new Float(r.rating);
				result.put(r.item, (float) r.rating);
				counters.put(r.item, 1);
			} else {
				counters.put(r.item, counters.get(r.item) + 1);
				result.put(r.item, result.get(r.item) + r.rating);
			}
		}
		// Divide by number of ratings
		for (Integer item : result.keySet()) {
			result.put(item, result.get(item) / (float) counters.get(item));
		}
		return result;
	}

	// =====================================================================================

	/**
	 * Calculates the past like statements of a user. Liked items are those
	 * which were rated above (or exactly as) the user's average
	 * 
	 * @return
	 */
	public static Map<Integer, Set<Integer>> getPastLikedItemsOfUsers(
			DataModel dm) {
		Map<Integer, Set<Integer>> result = new HashMap<Integer, Set<Integer>>();

		// Get the user averages first
		Map<Integer, Float> userAverages = dm.getUserAverageRatings();
		// go through the ratings and store things in the map
		for (Rating r : dm.getRatings()) {
			if (r.rating >= userAverages.get(r.user)) {
				Set<Integer> likedItems = result.get(r.user);
				if (likedItems == null) {
					likedItems = new HashSet<Integer>();
					result.put(r.user, likedItems);
				}
				likedItems.add(r.item);
			}
		}
		return result;
	}

	// =====================================================================================

	/**
	 * A method that calculates the overall rating average
	 * 
	 * @param dataModel
	 * @return the average rating
	 */
	public static double getGlobalRatingAverage(DataModel dataModel) {
		int cnt = 0;
		int total = 0;
		for (Rating rating : dataModel.getRatings()) {
			total += rating.rating;
			cnt++;
		}
		return total / (double) cnt;
	}
	
	
	/**
	 * The median rating
	 * @param dataModel - the data model
	 * @return the median rating
	 */
	public static float getGlobalMedianRating(DataModel dataModel) {
		// Sort the ratings in ascending order.
		if(dataModel.getRatings().isEmpty()){
			return Float.NaN;
		}
		List<Float> ratings = new ArrayList<>(dataModel.getRatings().size());
		for (Rating r : dataModel.getRatings()) {
			ratings.add(r.rating);
		}
		Collections.sort(ratings);
		return ratings.get(ratings.size() / 2);
	}
	
	/**
	 * Returns the frequencies per ratings level
	 * @param dataModel
	 * @return a map of rating frequencies
	 */
	public static Map<Float, Integer> getRatingFrequencies(DataModel dataModel) {
		Map<Float, Integer> result = new TreeMap<Float, Integer>();
		
		for (Rating r : dataModel.getRatings()) {
			Utilities101.incrementMapValue(result,r.rating);
		}
		
		return result;
	}
	

	// =====================================================================================
	/**
	 * This method removes all users (and their ratings) from the datamodel who
	 * have not issues at least minNumberOfRatingsPerUser ratings
	 * 
	 * @param dm
	 *            a data model
	 * @param minNumberOfRatingsPerUser
	 *            the threshold
	 * @return number of removed users
	 */
	public static int applyMinRatingsPerUserConstraint(DataModel dm,
			int minNumberOfRatingsPerUser) {
		Set<Rating> ratingsOfUser;
		int counter = 0;
		Set<Integer> userCopy = new HashSet<Integer>(dm.getUsers());
		for (Integer user : userCopy) {
			ratingsOfUser = new HashSet<Rating>(dm.getRatingsPerUser().get(user));
			if (ratingsOfUser != null && ratingsOfUser.size() < minNumberOfRatingsPerUser) {
				counter++;
				dm.removeUserWithRatings(user);
			}
		}
		
		//maybe some items are also obsolete now
		Set<Integer> items = new HashSet<>();
		for (Rating rating : dm.getRatings()) {
			items.add(rating.item);
		}
		dm.getItems().clear();
		dm.getItems().addAll(items);
		return counter;
	}

	// =====================================================================================

	/**
	 * A method that increments the counter value in a map. If no value exists,
	 * it adds 1. Otherwise we increment the value
	 * 
	 * @param map
	 * @param key
	 */
	public static <K> void incrementMapValue(Map<K, Integer> map, K key) {
		Integer existingValue = map.get(key);
		if (existingValue == null) {
			map.put(key, 1);
		} else {
			map.put(key, existingValue + 1);
		}
	}

	

	/**
	 * A method that increments the counter value in a map by a given value. If no value exists,
	 * it adds 1. Otherwise we increment the value
	 * 
	 * @param map
	 * @param key
	 * @param value
	 */
	public static <K> void incrementMapByGivenValue(Map<K, Integer> map, K key, int value) {
		Integer existingValue = map.get(key);
		if (existingValue == null) {
			map.put(key, value);
		} else {
			map.put(key, existingValue + value);
		}
	}

	/**
	 * A method that increments the counter value in a map by a given value. If no value exists,
	 * it adds 1. Otherwise we increment the value
	 * 
	 * @param map
	 * @param key
	 * @param value
	 */
	public static <K> void incrementMapByGivenValue(Map<K, Double> map, K key, double value) {
		Double existingValue = map.get(key);
		if (existingValue == null) {
			map.put(key, value);
		} else {
			map.put(key, existingValue + value);
		}
	}

	// =====================================================================================

	/**
	 * Accepts a real value and restricts it to the given bounds (saturation)
	 * 
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the allowed max
	 * @param value
	 *            the value
	 * @return the interval-restricted value
	 */
	public static float boundValue(float min, float max, float value) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}
	
	
	/**
	 * Apply the current bounds of the recommender
	 * @param prediction the prediction value
	 * @return the bound prediction value
	 */
	public static float applyRatingBounds(float prediction){
		return boundValue(Recommender101Impl.MIN_RATING, Recommender101Impl.MAX_RATING, prediction);
	}
	

	/**
	 * Returns a data model containing only a subset of the users
	 * 
	 * @param dm
	 *            the data model
	 * @param n
	 *            the number of users to retrain
	 * @return the updated data model
	 */
	public static DataModel sampleNUsers(DataModel dm, int n) {
		// Check if we have to do something
		if (n > dm.getUsers().size()) {
			return dm;
		}
		// Otherwise randomly pick a set of users to retain.
		List<Integer> chosenOnes = new ArrayList<Integer>();
		// make a copy of the existing ones
		List<Integer> copiedUsers = new ArrayList<Integer>(dm.getUsers());
		Collections.shuffle(copiedUsers);
		for (int i = 0; i < n; i++) {
			chosenOnes.add(copiedUsers.get(i));
		}
		// remove all the ones which are not the chosen ones
		for (Integer user : copiedUsers) {
			if (!chosenOnes.contains(user)) {
				// remove it.
				dm.removeUserWithRatings(user);
			}
		}
		return dm;
	}

	// --------------------------------------------------------------

	/**
	 * The method removes ratings as to decrease the density. It retains density
	 * * ratings of the ratings
	 * 
	 * @param dm
	 * @param density
	 *            the density (0 to 1)
	 * @return an updated data model
	 */
	public static DataModel applyDensityConstraint(DataModel dm, double density) {
		// Collect the ratings to remove
		int nBOfRatingsToRemove = (int) dm.getRatings().size()
				- (int) (dm.getRatings().size() * density);
		System.out.println("Will remove " + nBOfRatingsToRemove
				+ " (density = " + density + ")");
		List<Rating> ratingsList = new ArrayList<Rating>(dm.getRatings());

		Collections.shuffle(ratingsList);

		int cnt = 0;
		while (cnt < nBOfRatingsToRemove) {
			Rating r = ratingsList.get(cnt);
			dm.removeRating(r);
			cnt++;
		}
		dm.recalculateUserAverages();
		System.out.println("Retaining " + dm.getRatings().size() + " ratings.");
		return dm;

	}

	/**
	 * Writes the data to the data file (only user, item, rating, dummy>
	 * 
	 * @param dm
	 *            the modified dm
	 * @param filename
	 *            the outputfile
	 * @param inputfile
	 *            the original input file
	 */
	@SuppressWarnings("resource")
	public static void writeDataModelToFile(DataModel dm, String filename,
			String inputfile) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(inputfile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

		String line = reader.readLine();
		while (line != null) {
			if (line.startsWith("//")) {
				//System.out.println("Found a comment");
			}
			else {
				String[] tokens = line.split("\t");
				int user = Integer.parseInt(tokens[0]);
				int item = Integer.parseInt(tokens[1]);
	//			byte rating = Byte.parseByte(tokens[2]);
	
				// if there's a rating in the dm, write the line
				float r = dm.getRating(user, item);
				int posOfRest = line.indexOf("\t");
				posOfRest = line.indexOf("\t", posOfRest + 1);
				// Switch that on if there is an extra line to be considered.
				posOfRest = line.indexOf("\t", posOfRest + 1);
//				System.out.println("Pos of Rest: " + posOfRest);
				String restOfLine = "";
				if (posOfRest != -1) {
					restOfLine = "\t" + line.substring(posOfRest + 1, line.length());
				}
				if (r != -1) {
					bw.write(user + "\t" + item + "\t" + r + restOfLine + "\n");
				}
			}
			line = reader.readLine();
		}
		bw.close();
	}
	
	/**
	 * Calculates the variance of a set of ratings
	 * @return the variance of a set of ratings
	 */
	public static double getRatingVariance(Set<Rating> ratings) {
		double mean = 0.0;
		
		for (Rating r : ratings) {
			mean += r.rating;
		}
		mean = mean / ratings.size();
		
		double sum = 0.0;
		for (Rating r : ratings) {
			sum += Math.pow(mean - r.rating,2);
		}
		return sum/ratings.size();
	}

	/**
	 * A method that returns the variances for each items
	 * @param dm the data model
	 * @return the variances
	 */
	public static Map<Integer, Float> getItemRatingVariances(DataModel dm) {
		
		Map<Integer, Float> result = new HashMap<Integer, Float>();
		Map<Integer, Set<Rating>> ratingsPerItem = Utilities101.getRatingsOfItems(dm.getRatings());
		
		for (Integer item : dm.getItems()) {
			Set<Rating> itemRatings = ratingsPerItem.get(item);
			if (itemRatings != null) {
				float variance = (float) Utilities101.getRatingVariance(itemRatings);
				result.put(item, variance);
			}
		}
		return result;
	}
	
	
	/**
	 * Organizes the ratings per item
	 * @param ratings the set of ratings
	 * @return the mapped set
	 */
	public static Map<Integer, Set<Rating>> getRatingsOfItems(Set<Rating> ratings) {
		// Get the ratings for each item
		Map<Integer, Set<Rating>> ratingsPerItem = new HashMap<Integer, Set<Rating>>();
		for (Rating r : ratings) {
			Set<Rating> set = ratingsPerItem.get(r.item);
			if (set == null) {
				set = new HashSet<Rating>();
				ratingsPerItem.put(r.item, set);
			}
			set.add(r);
		}
		return ratingsPerItem;

	}
	
	  /**
	   * Calculates the dot product of two vectors.
	   * 
	   * @param first The first vector.
	   * @param second The second vector.
	   * @return The dot product.
	   */
	  public static double dot( double[] first, double[] second )
	  {
	    double accum = 0.0;
	    for ( int i = 0, n = first.length; i < n; i++ )
	      accum += first[ i ] * second[ i ];
	    return accum;
	  }

	  
	  /**
	   * Creates a string representation of an array
	   */
	  public static <E> String printArray (E[] a) {
		  StringBuffer result = new StringBuffer("[");
		  for (int i=0;i<a.length;i++) {
			  result.append(a[i]);
			  if (i < a.length-1) {
				  result.append(",");
			  }
		  }
		  result.append("]");
		  return result.toString();
	  }
	  
	  /**
	   * Creates a string representation of an array
	   */
	  public static String printArray (double[] a) {
		  StringBuffer result = new StringBuffer("[");
		  for (int i=0;i<a.length;i++) {
			  result.append(a[i]);
			  if (i < a.length-1) {
				  result.append(",");
			  }
		  }
		  result.append("]");
		  return result.toString();
	  }
	  
		/**
		 * Returns a string representation of the map sorted by keys
		 * @param map
		 * @return the sorted map as string
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static String printMapSorted(Map map) {
			StringBuffer result = new StringBuffer();
			List keys = new ArrayList(map.keySet());
			Collections.sort(keys);
			for (Object key : keys) {
				result.append(key + "=" + map.get(key) + ", ");
			}
			return result.toString().substring(0,result.length()-1);
		}

		// =====================================================================================

		/**
		 * A pretty printer that removes the package names
		 * 
		 * @param configurationString
		 * @return
		 */
		public static String removePackageQualifiers(String configurationString) {

			// Check if there have been parameters
//			System.out.println("ConfigurationString: " + configurationString);
			int idx = configurationString.indexOf(ClassInstantiator.SEPARATOR);
			// if not, remove everything up to the last dot.
			// if there is a dot
			if (idx == -1) {
				int idx2 = configurationString.lastIndexOf(".");
				// no package name
				if (idx2 == -1) {
					return configurationString;
				} else {
					// return everything behind the last dot
					return configurationString.substring(idx2 + 1,
							configurationString.length());
				}
			} else { // there are parameters and there might be dots afterwards
						// find the last dot in the string before the separator
				String leftPart = configurationString.substring(0, idx);
				int idx2 = leftPart.lastIndexOf(".");
				if (idx2 == -1) {
					// no packages, return everything
					return configurationString;
				} else {
					// return everything behind the last package delimiter
					return configurationString.substring(idx2 + 1,
							configurationString.length());
				}
			}
		}
		
		/**
		 * Calculates the Gini index // http://web.neuestatistik.de/inhalte_web/content /MOD_22410/html/comp_22748.html
		 * 
		 * @param bins
		 *            the bins in ascending order
		 * @return the concentration index between 0 and 1-1/n
		 */
		public static float calculateGini(long[] bins) {
			// pn = total number of ratings
			double pn = 0;
			double qn = 0;
			int cnt = 1;
			// calculate numbers
			for (long binCnt : bins) {
				pn += binCnt;
				qn += cnt * binCnt;
				cnt++;
			}
			double gini = 1 / (double) bins.length * (((2 * qn) / pn) - 1) - 1;
			//System.out.println("n = "+bins.length);
			//System.out.println("qn = "+qn);
			//System.out.println("pn = "+pn);
			return (float) gini;
		}
		
		/**
		 * Calculates the normalized Gini index // http://web.neuestatistik.de/inhalte_web/content /MOD_22410/html/comp_22748.html
		 * 
		 * @param bins
		 *            the bins in ascending order
		 * @return the normalized concentration index between 0 and 1
		 */
		public static float calculateNormalizedGini(long[] bins) {
			int n = bins.length;
			if (n <= 1) return (float) 0;
			double normGini = calculateGini(bins) * (n/(n-1));
			return (float) normGini;
		}

		
		// =====================================================================================
		/**
		 * This method removes items from the datamodel that
		 * have not at least minNumberOfRatingsPerItem ratings
		 * 
		 * @param dm
		 *            a data model to be modified
		 * @param minNumberOfItemsPerUser
		 *            the threshold
		 *  @return number of removed items
		 */
		public static int applyMinRatingsPerItemConstraint(DataModel dm, int minNumberOfRatingsPerItem) {
			
			int counter = 0;
			Map<Integer,Integer> ratingsPerItem = calculateRatingsPerItem(dm);
			Set<Rating> ratings = new HashSet<Rating>(dm.getRatings());
			
			for (Rating r : ratings){
				if ( ratingsPerItem.get(r.item) < minNumberOfRatingsPerItem){
					dm.removeRating(r);
				}
			}
			
			for (Entry<Integer, Integer> entry : ratingsPerItem.entrySet()) {
				if(entry.getValue()<minNumberOfRatingsPerItem){
					dm.getItems().remove(entry.getKey());
					counter++;
				}
			}
			
			return counter;
		}
		
		
		/**
		 * Determines, if an item is relevant for the user or not. An item is relevant when
		 * it is above a defined threshold, which is either the user's average or (in case
		 * the parameter is set) if it is x percent above the user's average.
		 * Alternatively, if the minRatingForRelevance parameter is set, this is used as 
		 * threshold
		 * @param item the item id
		 * @param user the user id
		 * @param dataModel the data model to use
		 * @param trainingData 
		 * @return true if the item is relevant
		 */
		public static boolean isItemRelevant(int item, int user, DataModel dataModel) {
			boolean result = false;
			if (Recommender101Impl.PREDICTION_RELEVANCE_MIN_RATING_FOR_RELEVANCE > 0) {
				// a variant
				float rating = dataModel.getRating(user, item);
//				System.out.println("Rating in testset was: " + rating);
//				System.out.println("min relevance: " + Recommender101Impl.PREDICTION_RELEVANCE_MIN_RATING_FOR_RELEVANCE);
				if (rating >= Recommender101Impl.PREDICTION_RELEVANCE_MIN_RATING_FOR_RELEVANCE) {
					return true;
				}
				else {
					return false;
				}
			}
			// Otherwise -> use some percentage threshold
			float userAvg = dataModel.getUserAverageRating(user);
			float rating = dataModel.getRating(user, item);
			double threshold = userAvg;
			
			if (Recommender101Impl.PREDICTION_RELEVANCE_MIN_PERCENTAGE_ABOVE_AVERAGE > 0) {
				threshold = threshold + (threshold * (Recommender101Impl.PREDICTION_RELEVANCE_MIN_PERCENTAGE_ABOVE_AVERAGE/(double) 100)); 
			}
			if (rating >= threshold) {
				result = true;
			}
			return result;
		}

		
		// =====================================================================================
		
		/**
		 * Converts the property string of evaluation methods to a list
		 * @param evaluators
		 * @return 
		 */
		public static List<String> getEvaluatorList(String evaluators) {
			ArrayList<String> evals = new ArrayList<String>();
			
			String[] methods = evaluators.split(",");
			
			for (String method : methods){
				evals.add(removePackageQualifiers(method.trim()));
			}
			
			return evals;
		}
		
		
		
		/**
		 * Downloads a file to a directory
		 * @param targetDirectory
		 * @param tempFile
		 * @return true, if the download was successful
		 */
		public static boolean downloadFile(String targetDirectory, String tempFile, String downloadURL) {
			try {
				// Download binary
				FileOutputStream os = new FileOutputStream(targetDirectory + tempFile);
				URL url = new URL(downloadURL);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					byte tmp_buffer[] = new byte[4096];
					InputStream is = conn.getInputStream();
					int n;
					while ((n = is.read(tmp_buffer)) > 0) {
						os.write(tmp_buffer, 0, n);
						os.flush();
					}
					os.close();
				} else {
					try {
						os.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					throw new IllegalStateException("HTTP response: " + responseCode);
				}

			} catch (Exception e) {
				System.err.println("Cound not download or process file: "
						+ e.getMessage());
				return false;
			}

			return true;		
		}
		
		/**
		 * Copies a stream 
		 * @param in
		 * @param out
		 * @throws IOException
		 */
		public static final void copyInputStream(InputStream in, OutputStream out)
				  throws IOException
		  {
		    byte[] buffer = new byte[1024];
		    int len;
		
		    while((len = in.read(buffer)) >= 0)
		      out.write(buffer, 0, len);
		
		    in.close();
		    out.close();
		  }
		
		/**
		 * Extracts a given file name from a zip file and deletes the zip file afterwards
		 * @param targetDirectory the directory where the file is
		 * @param sourceFile the zip file
		 * @param fileToExtract the file to extract (has to be specified with fullly qualified name including subdirectories)
		 * @param extractedFilename the target name
		 * @return true if the extraction was sucessful
		 */
		public static boolean extractFileFromZip(String targetDirectory, String sourceFile, String fileToExtract, String extractedFilename) {
			try {
				// Unzip the file
				@SuppressWarnings("resource")
				ZipFile zipFile = new ZipFile(targetDirectory + sourceFile);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					if (entry.getName().equals(fileToExtract)) {
//						System.out.println("Found entry: " + entry.getName());
						// create the output file
						Utilities101.copyInputStream(zipFile.getInputStream(entry),
						           new BufferedOutputStream(new FileOutputStream(targetDirectory + extractedFilename)));
						System.out.println("Created input file " + extractedFilename);
						break;
					}
				}
				// delete unnecessary stuff
				File zipfile = new File(targetDirectory + sourceFile);
				if (zipfile.exists()) {
					zipfile.delete();
				}
				
				return true;
			} 
			catch (Exception e) {
				System.out.println("Could not extract file "  + fileToExtract + " from " + sourceFile);
				return false;
			}
		}
		
	/**
	 * Get the value of a parameter (syntax --paramName=param)
	 * 
	 * @param name
	 *            the name of the parameter (case insensitive)
	 * @param args
	 *            the arguments
	 * @return the value if the parameter exists or null otherwise
	 */
	public static String getCommandLineArgument(String name, String[] args) throws Exception {
		String result = null;
		for (String arg : args) {
			try {
				if (arg.startsWith("--")) {
					String[] tokens = arg.substring(2,arg.length()).split("=");
					if (tokens[0].equalsIgnoreCase(name)) {
						
						return tokens[1];
					}
				}
			}
			catch (Exception e) {
				throw new Exception ("Illegal parameter syntax : " + arg);
			}
		}
		
		return result;
	}
	
	/**
	 * Get the value of a parameter (syntax --paramName=param)
	 * 
	 * @param name
	 *            the name of the parameter (case insensitive)
	 * @param args
	 *            the arguments
	 * @return the value if the parameter exists or null otherwise
	 */
	public static Integer getCommandLineArgumentAsInt(String name, String[] args) throws Exception {
		String result = null;
		result = getCommandLineArgument(name, args);
		if (result == null) {
			return null;
		}
		Integer r = null;
		try {
			r = Integer.parseInt(result);
		}
		catch (Exception e) {
			System.out.println("Parameter name is not an int (value: " + result + ")");
		}
		return r;
	}

	
	/**
	 * Returns the last parameter or null if none existed (no --prefix allowed)
	 * @param args the parameter list
	 * @return the parameter, or null if none exists
	 */
	public static String getLastCmdLineParameter(String[] args) {
		String result = null;
		try {
			String value = args[args.length-1];
			if (!value.startsWith("--")) {
				return value;
			}
		}
		catch (Exception e) { 
			return null;
		}
		
		return result;
	}
	
	/**
	 * Loads the feature weights into memory. Features weights are stored in a map from item-IDs to feature ID to weight
	 * Could be made more memory efficient
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public static Map<Integer, Map<Integer, Double>> loadFeatureWeights(String filename) throws Exception {
		HashMap<Integer, Map<Integer, Double>> result = new HashMap<Integer, Map<Integer, Double>>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = reader.readLine();
		String[] tokens;
		String[] entry;
		String itemid;
		HashMap<Integer, Double> newEntry;
		int idx;
		int cnt = 0;
		while (line != null) {
			cnt++;
			// Expected File structure
			// item-id;feature-id:weight feature-id weight
			// get the filename
			idx = line.indexOf(';');
			itemid = line.substring(0,idx);
			line = line.substring(idx+1,line.length());
//			idx = filename.lastIndexOf('/');
//			filename = filename.substring(idx+1,filename.length());
			newEntry = new HashMap<Integer, Double>();
			tokens = line.split(" ");
			for (String token: tokens) {
				if (token.length() > 0) {
					entry = token.split(":");
//					System.out.println("Entry: " + entry[0] + " " + entry[1]);
					newEntry.put(Integer.parseInt(entry[0]), Double.parseDouble(entry[1]));
				}
			}
			// remove the prefix first.
//			filename = filename.substring(2,filename.length());
//   		System.out.println("Filename: " + itemid);
//   		System.out.println("loading feature weight: " + newEntry);
			result.put(Integer.parseInt(itemid),newEntry);
			line = reader.readLine();
		
		}
		reader.close();
//		System.out.println("Loaded " + result.size() + " feature weights:\n" + result);
		return result;
	}
	
	/**
	 * Saves a map of (per-item) feature weights into a file with an agreed upon line structure
	 * @param filename
	 * @param tfidfVectors
	 * @throws Exception
	 */
	public static void createFeatureWeightsFile(String filename, Map<Integer, Map<Integer, Double>> tfidfVectors) throws Exception {
		try(PrintWriter pw = new PrintWriter(new FileWriter(filename))){
			// item-id;feature-id:weight feature-id weight
			tfidfVectors = Utilities101.sortByKeyAscending(tfidfVectors);
			for (Entry<Integer, Map<Integer, Double>> entry : tfidfVectors.entrySet()) {
				pw.print(entry.getKey());
				pw.print(";");
				Map<Integer, Double> featureMap = Utilities101.sortByKeyAscending(entry.getValue());
				for (Entry<Integer, Double> feature : featureMap.entrySet()) {
					pw.print(" ");
					pw.print(feature.getKey());
					pw.print(":");
					pw.print(feature.getValue());
				}
				pw.println();
			}
		}
	}
	
	
	/**
	 * Calculates the cosine similarity between two weight vectors. 
	 * Lists are generated first and empty fields filled with zeros
	 * @param map1
	 * @param map2
	 * @param wordlist used for better debugging
	 * @return the similarity or double.NaN if the value could not be calculated (because of missing content data)
	 */
	@SuppressWarnings("unused")
	public static double cosineSimilarity(Map<Integer, Double> map1, Map<Integer, Double> map2, List<String> wordlist) {
//		System.out.println("map1: " + map1 + " \nmap 2: " + map2);
		if (map1 == null || map2 == null) {
			return Double.NaN;
		}
		
		// create the lists
		List<Double> L1 = new ArrayList<Double>();
		List<Double> L2 = new ArrayList<Double>();
		// initialize the lists
		for (String word : wordlist)  {
			L1.add(0.0);
			L2.add(0.0);
		}
		// copy list one
		for (Integer pos : map1.keySet()) {
			L1.set(pos, map1.get(pos));
		}
		// copy list two
		for (Integer pos : map2.keySet()) {
			L2.set(pos, map2.get(pos));
		}
		Double[] arr1 = L1.toArray(new Double[L1.size()]);
		Double[] arr2 = L2.toArray(new Double[L2.size()]);
		double result = dot( arr1, arr2 ) / ( Math.sqrt( dot( arr1, arr1 ) ) * Math.sqrt( dot( arr2, arr2 ) ) );
		
		
		return result;
	}

	// =====================================================================================

	/**
	 * Returns the dot product of two arrays
	 * @param arr1
	 * @param arr2
	 * @return the dot product
	 */
	public static double dot(Double[] arr1, Double[] arr2) {
		double result = 0.0;
    for ( int j = 0, n = arr1.length; j < n; j++ ) {
      result += arr1[j] * arr2[j];
    }
    return result;
	}
	
	
	// =====================================================================================

	/**
	 * Loads the word list from the file system into memory
	 * @return a list of words
	 * @throws Exception
	 */
	public static List<String> loadwordlist(String filename) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = reader.readLine();
		while (line != null) {
			result.add(line);
			line = reader.readLine();
		}
		reader.close();
		return result;
	}
	
	// =====================================================================================
	
	/**
	 * {@link #createCosineSimilaritiesFile(String, String, Map, List, int)}
	 * Uses the default file name 'cos-sim-vectors.txt'.
	 * @param dataDirectory
	 * @param featureWeights
	 * @param wordlist
	 * @param nbOfThreads
	 * @throws Exception
	 */
	public static void createCosineSimilaritiesFile(String dataDirectory, Map<Integer, Map<Integer, Double>> featureWeights, List<String> wordlist, int nbOfThreads) throws Exception {
		createCosineSimilaritiesFile(dataDirectory, "cos-sim-vectors.txt", featureWeights, wordlist, nbOfThreads);
	}
	
	/**
	 * Helper method that calculates cosine similarities for item pairs in an TF-IDF vector file and stores them in a separate file
	 * As a convention, in this file each item ID has a list of items with greater ID that holds the corresponding cosine similarity for the item pair 
	 * @param dataDirectory directory in which all files for this calculation are located
	 * @param featureWeights a nestled map of feature weights
	 * @param wordlist a list of terms for the feature weight content
	 * @param nbOfThreads how many thread shall be used for the creation of the file
	 */
	public static void createCosineSimilaritiesFile(String dataDirectory, String fileName, Map<Integer, Map<Integer, Double>> featureWeights, List<String> wordlist, int nbOfThreads) throws Exception {
		Debug.log("Calculating cosine similarities, this can take a while");
		
		//keep track of appropriate line number
		int lineNo = 0;
		
		//Get ExecutorService from Executors utility class, thread pool size is n
        ExecutorService executor = Executors.newFixedThreadPool(nbOfThreads);
        //create a list to hold the Future object associated with Callable
        List<Future<SimpleEntry<Integer,String>>> futures = new ArrayList<Future<SimpleEntry<Integer,String>>>();
		
		// compare each entry with all other entries
		for (Map.Entry<Integer, Map<Integer, Double>> entry1 : featureWeights.entrySet()) {
			
			//add the assignment to the thread pool
			Callable<SimpleEntry<Integer,String>> callable = new CosineCallable(lineNo, entry1, featureWeights, wordlist);
			futures.add(executor.submit(callable));
			
			lineNo++;
		}
		
		//keep track of count
		long count = 0;
		long lastPercent = 0;
			
		//Retrieve the results
		Map<Integer, String> lines = new HashMap<Integer, String>();
		for(Future<SimpleEntry<Integer,String>> f : futures){
			//The next line waits, so we will only be done with this for loop when all results are in
			SimpleEntry<Integer,String> result = f.get();
			
			//Log the progress
			count++;
			long percent; //our granularity is xxx.xx%
			if((percent=count*10000/featureWeights.size())!=lastPercent){
				lastPercent = percent;
				Debug.log("Progress: " + String.format("%.2f", percent*1d/100) + " %");
			}
			
			lines.put(result.getKey(), result.getValue());
		}
		
		//shut down the executor service now
        executor.shutdown();
        
        Debug.log("Finished. Now writing the file.");
        
        // prepare file for output; will be overwritten if it already exists
     	PrintWriter pWriter = new PrintWriter(new FileWriter(dataDirectory + "/" + fileName));
     	
     	//write the lines
     	Map<Integer, String> sortedLines = sortByKeyAscending(lines);
        for (String line : sortedLines.values()) {
        	pWriter.println(line);
		}
        
		// close output file
		pWriter.close();
		
		
		Debug.log("Cosine similarities successfully saved to file: " + dataDirectory + "/cos-sim-vectors.txt");
	}
	
	/**
	 * Encapsulates the cosine creation process to make it multi threaded
	 * @author Jugovac
	 *
	 */
	public static class CosineCallable implements Callable<SimpleEntry<Integer, String>>{

		private int lineNumber;
		private Entry<Integer, Map<Integer, Double>> primaryElement;
		private Map<Integer, Map<Integer, Double>> otherFeatureWeights;
		private List<String> wordlist;

		public CosineCallable(int lineNumber, Map.Entry<Integer, Map<Integer, Double>> primaryElement, 
				Map<Integer, Map<Integer, Double>> otherFeatureWeights, List<String> wordlist){
					this.lineNumber = lineNumber;
					this.primaryElement = primaryElement;
					this.otherFeatureWeights = otherFeatureWeights;
					this.wordlist = wordlist;
			
		}
		
		/**
		 * Does the actual work of creating exactly one line for the cos-sim file
		 */
		@Override
		public SimpleEntry<Integer, String> call() throws Exception {
			StringBuilder b = new StringBuilder();
			// write first (smaller) key for this line
			b.append(primaryElement.getKey() + ";");
				
			for (Map.Entry<Integer, Map<Integer, Double>> entry2 : otherFeatureWeights.entrySet()) {
					
				// make sure we don't compare an entry with itself and that we only compare entries once, and that the second entry is always greater
				if (primaryElement.getKey() < entry2.getKey()) {
						

					// calculate cosine similarity for the values of the key pair
					double similarity = Utilities101.cosineSimilarity(primaryElement.getValue(), entry2.getValue(), wordlist);
					
					// double-check if calculation was successful
					if (!Double.isNaN(similarity)) {
	
						// write the second key and the similarity for the key pair
						b.append(" " + entry2.getKey() + ":" + similarity);
					}
				}
			}
			
			//return the line number and the corresponding line for the cosine file
			return new SimpleEntry<>(lineNumber, b.toString());
		}
		
	}
	
	// =====================================================================================
	
	/**
	 * Helper method that loads pair cosine similarities from file into memory
	 * @param filename
	 * @return a nestled map of cosine similarities associated with pairs of items; the item ID of outer map's key is always smaller than the inner map's key
	 */
	public static Map<Integer, Map<Integer, Double>> loadCosineSimilarities(String filename) {
		
		Map<Integer, Map<Integer, Double>> result = new HashMap<Integer, Map<Integer, Double>>();
		Map<Integer, Double> newEntry = new HashMap<Integer, Double>();
		Integer keyItem;
		String[] tokens;
		String[] entry;
		
		Debug.log("Loading cosine similarities from file: " + filename);
		
		try {
			BufferedReader reader;
			ZipFile zipFile = null;
			// initialize buffered reader
			if(filename.endsWith(".zip")){
				Debug.log("Assuming file is zipped. Trying to access zipped content.");
				zipFile = new ZipFile(filename);

				InputStream inputStream = zipFile.getInputStream(zipFile.entries().nextElement());

				reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			}else{
				reader = new BufferedReader(new FileReader(filename));
			}
			
			
			String line = reader.readLine();
			
			// read and load file content
			while (line != null) {
				
				// extract data from the line
				// expects file line structure: itemA; itemB:cosineSimilarity with itemA < itemB
				keyItem = Integer.parseInt(line.substring(0, line.indexOf(';')));
				line = line.substring(line.indexOf(';') + 1,line.length());
				
				// create a new entry in the result map
				newEntry = new HashMap<Integer, Double>();
				tokens = line.split(" ");
				for (String token : tokens) {
					if (token.length() > 0) {
						entry = token.split(":");
						newEntry.put(Integer.parseInt(entry[0]), Double.parseDouble(entry[1]));
					}
				}
				result.put(keyItem, newEntry);
				
				// get next line
				line = reader.readLine();
			}
			
			// close reader and return result
			reader.close();
			if(zipFile!=null){
				zipFile.close();
			}
			Debug.log("Successfully loaded cosine similarities for " + result.size() + " items");
			return result;
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates a cosine similarities file from an already precomputed hashmap of cosine similarities.
	 * The map has to be sorted. smallest item (by id) has to be matched with all others. largest item has to be matched with none.
	 * This will create a zip file; so please name your file "xyz.zip".
	 * @param outFileName
	 * @param consines
	 * @throws IOException 
	 */
	public static void createCosineSimilaritiesFile(String outFileName,
			Map<Integer, Map<Integer, Double>> consines) throws IOException {
		
		File file = new File(outFileName);
		file.getParentFile().mkdirs();
		
		FileOutputStream fos = new FileOutputStream(file);
		ZipOutputStream zos = new ZipOutputStream(fos);
		ZipEntry ze= new ZipEntry("sim.txt");
		zos.putNextEntry(ze);
		
		PrintWriter pw = new PrintWriter(zos);
		for (Entry<Integer, Map<Integer, Double>> entry : consines.entrySet()) {
			pw.print(entry.getKey() + ";");
			for (Entry<Integer, Double> entry2 : entry.getValue().entrySet()) {
				pw.print(" " + entry2.getKey() + ":" + entry2.getValue());
			}
			pw.println();
		}
		
		pw.flush();
		//pw.close();
		zos.closeEntry();
		zos.close();
	}

	
		
}
