/** DJ **/
package org.recommender101.recommender.baseline;

import it.unimi.dsi.fastutil.ints.Int2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.recommender101.data.Rating;
import org.recommender101.gui.annotations.R101Class;
import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;
import org.recommender101.recommender.AbstractRecommender;
import org.recommender101.tools.Debug;
import org.recommender101.tools.Utilities101;

/**
 * Implements the most common baseline. A nearest neighbor method (user/user, item/item) with
 * Pearson correlation or Cosine Similarity as a metric
 * @author DJ
 *
 */
@SuppressWarnings("serial")
@R101Class (name="Nearest Neighbors", description="Implements the most common baseline. A nearest neighbor method (user/user, item/item) with Pearson correlation or Cosine Similarity as a metric")
public class NearestNeighbors extends AbstractRecommender {
	
	/**
	 * Determines how the similarity is to be computed
	 * @author MJ
	 *
	 */
	protected enum SimilarityMetric{
		Pearson,
		Cosine,
		Jaccard
	}

	
	public Map<Integer, Float> averages;
	
	// Number of neighbors to consider (default 30)
	protected int nbNeighbors = 30;
	
	// The minimum similarity threshold
	protected double simThreshold = 0.0;
	

	// Some minimum overlap (co-rated items)
	protected int minRatingOverlap = 3;
	
	/**
	 * As a default, we will use the user-based method
	 */
	protected boolean itemBased = false;
	
	/**
	 * As a default we will use Pearson similarity
	 */
	protected SimilarityMetric similarityMetric = SimilarityMetric.Pearson;
	
	/**
	 * The minimum number of neighbors we need
	 */
	protected int minNeighbors = 1;
	
	/**
	 * Stores the similarities user-id-> map of other users and their similarities
	 */ 
	public Map<Integer,Map<Integer, Double>> theSimilarities = new Int2ObjectOpenHashMap<>();


	public Map<Integer, Set<Rating>> ratingsPerItem;
	
	
	/**
	 * Predict the rating based on the neighbors opinions.
	 * Use a classical weighting scheme and n neighbors
	 */
	@Override
	public synchronized float predictRating(int user, int item) {
		// Iterate over all users and rank them
		// A map of similarities
		
		Map<Integer, Double> similarities;
		if (itemBased) {
			similarities = this.theSimilarities.get(item);
		}
		else {
			similarities = this.theSimilarities.get(user);
		}
		// Check if we have enough neighbors
		if (similarities == null || (similarities.size() < this.minNeighbors)) {
			return Float.NaN;
		}
		// The prediction function.
		// Take the user's average and add the weighted deviation of the neighbors.
		Float objectAverage;
		if (itemBased) {
			objectAverage = this.averages.get(item);
			if (objectAverage == null) {
				return Float.NaN;
			}
		}
		else {
			objectAverage = averages.get(user);
			if (objectAverage == null) {
				return Float.NaN;
			}
		}
		
		
		// go through all the neighbors 
		double totalBias = 0;
		double totalSimilarity = 0;
		for (Integer otherObject : similarities.keySet()) {
			float neighborRating;
			if (itemBased) {
				neighborRating = dataModel.getRating(user, otherObject);
			}
			else { //user-based
				neighborRating = dataModel.getRating(otherObject, item);
			}
			if (!Float.isNaN(neighborRating) && neighborRating != -1) {
				double neighborBias = neighborRating - averages.get(otherObject); 
				neighborBias = neighborBias * similarities.get(otherObject);
				totalBias += neighborBias;
				totalSimilarity += Math.abs(similarities.get(otherObject));
			}
		}
		
		return (float) (objectAverage + (totalBias / totalSimilarity));
	}

	// =====================================================================================
	/**
	 * Returns the set of ratings of a given item id 
	 * @param itemid the itemid
	 * @return the ratings, or null if no ratings exist
	 */
	public Set<Rating> getRatingsPerItem(Integer itemid) {
		return ratingsPerItem.get(itemid);
	}

	// =====================================================================================

	/**
	 * Pre-calculates the ratings per item from the data model
	 * @return the map of item-ids to ratings
	 */
	public Map<Integer, Set<Rating>> calculateRatingsPerItem() {
		Map<Integer, Set<Rating>> result = new HashMap<Integer, Set<Rating>>();
		
		Set<Rating> ratings = dataModel.getRatings();
		
		Set<Rating> ratingsOfItem;
		for (Rating r : ratings) {
			ratingsOfItem = result.get(r.item);
			if (ratingsOfItem == null) {
				ratingsOfItem = new HashSet<Rating>();
				result.put(r.item,ratingsOfItem);
			}
			ratingsOfItem.add(r);
		}
		return result;
	}

	// =====================================================================================
	
	/**
	 * This method recommends items.
	 */
	@Override
	public List<Integer> recommendItems(int user) {
		if(itemBased){
			return recommendItemsByRatingPrediction(user);
		}
		
		//we do a custom "recommendItemsByRatingPrediction" because we can save some time if we don't predict for items 
		//which are irrelevant anyway because the neighbors haven't rated them
		Map<Integer, Double> similarities = this.theSimilarities.get(user);
		
		// Check if we have enough neighbors
		if (similarities == null || (similarities.size() < this.minNeighbors)) {
			Debug.error("kNN: No neighbors");
			return new ArrayList<>();
		}
		//check which items are actually relevant for the computation because the neighbors have rated them
		Set<Integer> eligableItems = new HashSet<>();
		for (Integer neighbor : similarities.keySet()) {
			Set<Rating> ratingsOfNeighbor = dataModel.getRatingsOfUser(neighbor);
			for (Rating rating : ratingsOfNeighbor) {
				eligableItems.add(rating.item);
			}
		}
		
		//remove the items that the current user has rated
		Set<Rating> ratingsOfUser = dataModel.getRatingsOfUser(user);
		for (Rating rating : ratingsOfUser) {
			eligableItems.remove(rating.item);
		}
		
		//calculate the predictions
		Map<Integer, Float> predictions = new HashMap<>();
		for (Integer item : eligableItems) {
			float pred = predictRating(user, item);
			if (!Float.isNaN(pred)) {
				predictions.put(item, pred);
			}
		}
		if(predictions.isEmpty()){
			Debug.error("kNN: Empty recommendation list");
		}		
		
		return new ArrayList<>(Utilities101.sortByValueDescending(predictions).keySet());
	}

	/**
	 * Initialization: Compute the user averages
	 */
	@Override
	public void init() throws Exception {
		computeAverages();
		// Pre-compute the similarities between all users first
		double sim = Double.NaN;
		
		int similaritiesToCompute; 
		List<Integer> objects;
		if (itemBased) {
			similaritiesToCompute = (dataModel.getItems().size() * dataModel.getItems().size())/2;
			objects = new ArrayList<Integer>(dataModel.getItems());
		}
		else { //user-based
			similaritiesToCompute = (dataModel.getUsers().size() * dataModel.getUsers().size())/2; 
			objects = new ArrayList<Integer>(dataModel.getUsers());
		}
		Debug.log("NearestNeighbors: Calculating up to " + similaritiesToCompute + " similarities in the test set.. This may take some time.");
	
		Map<Integer, Double> currentMins = new HashMap<>();
		
		// sort in ascending order
		Collections.sort(objects);
		int counter = 0;
		int tenpercent = similaritiesToCompute / 10;
		for (int i=0;i<objects.size();i++) {
			for (int j=i+1;j<objects.size();j++) {
				counter++;
				int objI = objects.get(i);
				int objJ = objects.get(j);
				if (counter % tenpercent == 0) {
					Debug.log("Similarity computation at : " + Math.round(((counter  / (double) similaritiesToCompute * 100))) + " %");
				}
				sim = similarity(objI, objJ);
				if (!Double.isNaN(sim)) {
					if (sim > simThreshold) {
						
						Map<Integer, Double> objectSimilarites1 = theSimilarities.get(objI);
						if (objectSimilarites1 == null) {
							objectSimilarites1 = new Int2DoubleOpenHashMap();
							theSimilarities.put(objI, objectSimilarites1);
						}
						coditionallyAddToMap(objectSimilarites1, objI, objJ, sim, currentMins);
						
						// Copy things
						Map<Integer, Double> objectSimilarites2 = theSimilarities.get(objJ);
						if (objectSimilarites2 == null) {
							objectSimilarites2 = new Int2DoubleOpenHashMap();
							theSimilarities.put(objJ, objectSimilarites2);
						}
						coditionallyAddToMap(objectSimilarites2, objJ, objI, sim, currentMins);
					}
				}
			}
		}
		
		// go through all the user similarities and sort them
		for (Integer object : theSimilarities.keySet()) {
			Map<Integer, Double> sims = theSimilarities.get(object);
			theSimilarities.put(object, new Int2DoubleLinkedOpenHashMap(Utilities101.sortByValueDescending(sims)));
		}
		
		Debug.log("Nearest neighbors: Computed " + similaritiesToCompute + " similarities");
	}

	/**
	 * Compute the average rating values
	 */
	protected void computeAverages() {
		if (itemBased) {
			averages = Utilities101.getItemAverageRatings(dataModel.getRatings());
			ratingsPerItem = calculateRatingsPerItem();
		}
		else {
			averages = dataModel.getUserAverageRatings();
		}
	}
	
	
	/**
	 * Calculates the Pearson or cosine similarity for two objects. Returns Double.NaN if
	 * there are not enough co-rated items
	 * @param object1 the first object
	 * @param object2 the second object
	 * @return a similarity value between -1 and 1
	 */
	protected double similarity (Integer object1, Integer object2) {
		// Ratings to compare
		Set<Rating> ratings1 = null;
		Set<Rating> ratings2 = null;

		if (itemBased) {
			// Need the pre-computed sets of ratings per items.
			ratings1 = getRatingsPerItem(object1);
			ratings2 = getRatingsPerItem(object2);
		}
		else { //user-based
			ratings1 = dataModel.getRatingsPerUser().get(object1);
			ratings2 = dataModel.getRatingsPerUser().get(object2);
		}
		
		// Determine the ids of the co-rated items or the co-rating users in case of the item-based
		// approach
		Set<Integer> r1 = new HashSet<Integer>();
		Set<Integer> r2 = new HashSet<Integer>();
		
		if (itemBased) {
			for (Rating r : ratings1) {
				r1.add(r.user);
			}
			for (Rating r : ratings2) {
				r2.add(r.user);
			}
		}
		else { //user-based
			for (Rating r : ratings1) {
				r1.add(r.item);
			}
			for (Rating r : ratings2) {
				r2.add(r.item);
			}
			
		}
		
		// Calculate the overlap (intersection)
		// was rating all r1 before
		r1.retainAll(r2);
		
		if (r1.size() == 0 || r1.size() < this.minRatingOverlap) {
			return Double.NaN;
		}
		
		// calculate the similarity / correlation
		// item-based: objects are items, r1 are co-rated users
		// user-based: objects are users, r1 are co-rated items
		return calculateSimilarity(object1, object2, r1);
	}
	
	/**
	 * Only adds the similarity value between objects i and j to the list of object i 
	 * if (a) the list has less than nbNeighbors entries or
	 * (b) the similarity between i and j is higher than all the similarities 
	 * currently on record for i. In this case another similarity value is removed from i's list.
	 * @param similarites
	 * @param i
	 * @param j
	 * @param sim
	 * @param currentMins
	 */
	private void coditionallyAddToMap(Map<Integer, Double> similarites,
			int i, int j, double sim, Map<Integer, Double> currentMins) {
		Double min = currentMins.get(i);
		if(similarites.size()==nbNeighbors){
			//we already have enough -> determine if this item is better than all the others currently in the list
			if(min<sim){
				//we have a better item
				//search for the userId of the current minimum
				Comparator<Map.Entry<Integer, Double>> comp = new Comparator<Map.Entry<Integer,Double>>() {
					@Override
					public int compare(Map.Entry<Integer, Double> o1,
							Map.Entry<Integer, Double> o2) {
						return o1.getValue().compareTo(o2.getValue());
					}
				};
				Entry<Integer, Double> minObj = Collections.min(similarites.entrySet(), comp);
				
				similarites.remove(minObj.getKey());
				//search for the userId of the now new minimum
				minObj = Collections.min(similarites.entrySet(), comp);
				//set the new minimum (either the new item or our old runner-up minimum
				if(sim < minObj.getValue()){
					currentMins.put(i, sim);
				}else{
					currentMins.put(i, minObj.getValue());
				}
				similarites.put(j, sim);
			}
		}else{
			//we need to determine the current min
			similarites.put(j, sim);
			if(min == null || min>sim){
				currentMins.put(i, sim);			
			}
		}
	}
	
	
	/**
	 * An internal function (to be overwritten in a subclass) to calculate the Pearson correlation of two 
	 * users
	 * @param object1 id of item 1 (item-based) or user 1 (user-based)
	 * @param object2 id of item 2 (item-based) or user 2 (user-based)
	 * @param overlap the set of co-rated users (item-based) or co-rated items (user-based)
	 * @return returns the similarity value;
	 */
	protected double calculateSimilarity(Integer object1, Integer object2, Set<Integer> overlap) {
		double result = Double.NaN;
		
		// Cosine similarity computation (and not adjusted cosine)
		if (similarityMetric==SimilarityMetric.Cosine) {
			int commonObjects = overlap.size();
			// get the ratings
			double[] ratings1 = new double[commonObjects];
			double[] ratings2 = new double[commonObjects];
			
			int i = 0;
			// copy the ratings into arrays
			for (Integer coRated : overlap) {
				if (itemBased) {				  // (user   , item   )
					ratings1[i] = dataModel.getRating(coRated, object1); 
					ratings2[i] = dataModel.getRating(coRated, object2);
				}
				else{ //user-based
					ratings1[i] = dataModel.getRating(object1, coRated);
					ratings2[i] = dataModel.getRating(object2, coRated);
				}
				i++;
			}
			result = Utilities101.dot( ratings1, ratings2) / ( Math.sqrt( Utilities101.dot( ratings1, ratings1 ) ) * Math.sqrt( Utilities101.dot( ratings2, ratings2 ) ) );
			return result;
		}
		// Use Pearson correlation
		else if (similarityMetric == SimilarityMetric.Pearson) {
			double mean1 = averages.get(object1);
			double mean2 = averages.get(object2);

			double numerator = 0.0;
			double squaredDev1 = 0.0;
			double squaredDev2 = 0.0;
			
			// Iterate through all and sum things up
			for (Integer coRated : overlap) {
				double rating1;
				double rating2;
				if (itemBased) {			  // (user, item   )
					rating1 = dataModel.getRating(coRated, object1);
					rating2 = dataModel.getRating(coRated, object2);
				}
				else{ //user-based
					rating1 = dataModel.getRating(object1, coRated);
					rating2 = dataModel.getRating(object2, coRated);
				}
				numerator += (rating1 - mean1) * (rating2 - mean2);
				squaredDev1 += Math.pow((rating1 - mean1),2);
				squaredDev2 += Math.pow((rating2 - mean2),2);
			}
			result = numerator / (Math.sqrt(squaredDev1) * Math.sqrt(squaredDev2));
			return result;
		}else{
			HashSet<Integer> all = new HashSet<>();
			//jaccard
			if (itemBased) {			  
				 Set<Rating> ratingsPerItem = getRatingsPerItem(object1);
				 for (Rating rating : ratingsPerItem) {
					 all.add(rating.item);
				 }
				 ratingsPerItem = getRatingsPerItem(object2);
				 for (Rating rating : ratingsPerItem) {
					 all.add(rating.item);
				 }
			}
			else{
				for (Rating rating : dataModel.getRatingsOfUser(object2)) {
					all.add(rating.item);
				}
				for (Rating rating : dataModel.getRatingsOfUser(object1)) {
					all.add(rating.item);
				}
			}

			return ((double)overlap.size()) / ((double)all.size());
		}
	}
	
	// =====================================================================================
	


	/**
	 * Setter for the factory
	 * @param n the max number of neighbors
	 */
	@R101Setting( displayName="Max Neighbors", description="The maximum number of neighbors",
			defaultValue="30", type=SettingsType.INTEGER, minValue=0)
	public void setNeighbors(String n) {
		this.nbNeighbors = Integer.parseInt(n);
	}
	
	/**
	 * Sets the similarity threshold
	 * @param s
	 */
	@R101Setting (displayName="Minimum Similarity", defaultValue="0.0", minValue=0, type=SettingsType.DOUBLE,
			description="Similarity Threshold")
	public void setMinSimilarity(String s) {
		this.simThreshold = Double.parseDouble(s);
	}
	
	/**
	 * Setter for the min overlap value
	 * @param overlap
	 */
	@R101Setting(displayName="Minimum overlap", description="Minimum overlap value",
			type=SettingsType.INTEGER, defaultValue="3", minValue=0)
	public void setMinOverlap(String overlap) {
		this.minRatingOverlap = Integer.parseInt(overlap);
	}

	/**
	 * Setter for the min number of neighbors
	 * @param min
	 */
	@R101Setting(displayName="Min Neighbors", description="The minimum number of neighbors",
			defaultValue="1", type=SettingsType.INTEGER, minValue=0)
	public void setMinNeighbors(String min) {
		this.minNeighbors = Integer.parseInt(min);
	}
	
	/**
	 * Set this flag to do item based computations
	 * @param itembased should be "true"
	 */
	@R101Setting(displayName="Item based", description="Enables item based computations",
			defaultValue="false", type=SettingsType.BOOLEAN)
	public void setItemBased(String itembased) {
		if ("true".equalsIgnoreCase(itembased)) {
			this.itemBased = true;
		}
	}
	
	/**
	 * Sets similarity metric to be used for the computation
	 * @param had to be one of "Pearson", "Cosine", or "Jaccard"
	 */
	@R101Setting (displayName="Similarity Metric", type=SettingsType.TEXT,
			defaultValue="Pearson", description="Sets similarity metric to be used for the computation" )
	public void setSimilarityMetric(String val) {
		similarityMetric = SimilarityMetric.valueOf(val);
	}

	// Get the minimum value for the similarity metric to consider the neighbor
	public double getSimThreshold() {
		return simThreshold;
	}

	// Set the min value for the similarity measure to consider the neighbor
	public void setSimThreshold(String simThreshold) {
		try {
			this.simThreshold = Double.parseDouble(simThreshold);
		}
		catch (Exception e) {
			System.out.println("simThreshold has to be a double");
		}
	}
	
	
	@Override
	public int getDurationEstimate() {
		return 8;
	}
	

	
}
