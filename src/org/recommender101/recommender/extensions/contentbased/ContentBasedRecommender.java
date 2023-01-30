/** DJ **/
package org.recommender101.recommender.extensions.contentbased;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.recommender101.data.Rating;
import org.recommender101.gui.annotations.R101Class;
import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;
import org.recommender101.recommender.AbstractRecommender;
import org.recommender101.recommender.baseline.PopularityAndAverage;
import org.recommender101.recommender.extensions.funksvd.FunkSVDRecommender;
import org.recommender101.recommender.extensions.slopeone.SlopeOneRecommender;
import org.recommender101.tools.Debug;
import org.recommender101.tools.Utilities101;


/**
 * A basic content-based recommender which calculates a user's profile based on the previously 
 * liked items and recommends items which are similar to the profile.
 * 
 * The input is a set of TF-IDF vectors as shown in the demo file (and produced by the word
 * vector tool of RapidMiner as well as file containing the words
 * 
 * We also include the word vector tool to show how word vectors can be generated and
 * stop words can be removed.
 * * 
 * @author DJ
 *
 */
@SuppressWarnings("serial")
@R101Class(name="Content-based Recommender", description="A basic content-based recommender which calculates a user's profile based on the previously liked items and recommends items which are similar to the profile.")
public class ContentBasedRecommender extends AbstractRecommender {

	/**
	 * Where do we expect the files in the default case
	 */
	public static String dataDirectory = "data/movielens/";
	
	/**
	 * The file where the word list is stored 
	 */
	public String wordListFile = "wordlist.txt";
	
	/**
	 * Where we expect the feature (TF-IDF or lsa weight) files
	 */
	public String featureWeightFile = "tfidf.txt";
	
	/**
	 * Where we expect the cosine similarities file
	 */
	public String cosineSimilaritiesFile = "cos-sim-vectors.zip";
	
	// The list of words
	List<String> wordlist;

	// The weights per word of a game: maps product IDs to a map of word-id and weights
	Map<Integer, Map<Integer, Double>> featureWeights;
	
	// The cosine similarities of each item pair
	Map<Integer, Map<Integer, Double>> cosineSimilarities;
	
	// The average profile vector per user
	Map<Integer, Map<Integer, Double>> userProfiles;

	// A map where we store what a user has liked in the past
	// Liked items are those that the user has rated above his own average in the past
	Map<Integer, Set<Integer>> likedItemsPerUser;
	
	// Remember the user averages for the prediction task
	Map<Integer, Float> userAverages;
	
	// Remember the item averages for the prediction task
	Map<Integer, Float> itemAverages;

	// How many neighbors should be used for the rating prediction
	int nbNeighborsForPrediction = 10;
	
	// Abstract Recommender as a fallback
	AbstractRecommender fallBackRec = null;

	// The fallback string
	String fallBack = null;
	
	
	/**
	 * The min similarity for predictions
	 */
	double simThresholdForPrediction = 0.0;
	
	//default is true for legacy reasons
	private boolean hideKnownItems = true;
	
	
	// =====================================================================================
	@Override
	public float predictRating(int user, int item) {
		// The default
		Float userAvg = this.userAverages.get(user);
		
		// We can do nothing about this user
		// The item default might be a fallback
		if (userAvg == null) {
			Float itemAVG = this.itemAverages.get(item);
			if (itemAVG != null) {
				System.out.println("Returning item average");
				return itemAVG;
			}
			else {
				System.out.println("No user and item average available");
				return Float.NaN;
			}
		}
		
		
		// The algorithm searches for the n most similar items of the given item and combines the ratings
		// the prediction is a weighted combination of the neighbor ratings.
		// Get the feature vector of the target item
		//Map<Integer, Double> targetItemVector = this.featureWeights.get(item);
		//Map<Integer, Double> otherItemVector;
		// go through all the items for which we have ratings
		Set<Rating> ratedItems = dataModel.getRatingsPerUser().get(user);
		
		// Default, if we have no data
		if (ratedItems.size() == 0) {
			return userAvg;
		}
		
		// Here's where we will store the similarities
		Map<Integer, Double> similarities = new HashMap<Integer, Double>();
		double similarity = 0;
		int keySmaller;
		int keyGreater;
		for (Rating r : ratedItems) {
			// Get the feature vector
			//otherItemVector = this.featureWeights.get(r.item);
			//similarity = Utilities101.cosineSimilarity(targetItemVector, otherItemVector, this.wordlist);
			
			// get the cosine similarity of the current pair if possible
			keySmaller = Math.min(item, r.item);
			keyGreater = Math.max(item, r.item);
			if ((cosineSimilarities.get(keySmaller) != null) && (cosineSimilarities.get(keySmaller).get(keyGreater) != null)) {
				similarity = cosineSimilarities.get(keySmaller).get(keyGreater);
			} else {
				similarity = Double.NaN;
			}
			
			if (Double.isNaN(similarity)) {
				similarity = 0.0;
			}
			similarities.put(r.item, similarity);
		}
		
		similarities = Utilities101.sortByValueDescending(similarities);
//		System.out.println("Similarities: " + similarities);
		
		double existingRatingsSum = 0;
		int counter = 0;
		double weightSum = 0;
		double similarityWeight = 0;
		double ratingSum = 0;
		for (Integer otherItem : similarities.keySet()) {
//			System.out.println("Using for prediction " + otherItem + ", user rating was: " + dataModel.getRating(user, otherItem));
			
			// remember the weight
			similarityWeight = similarities.get(otherItem);
			if (similarityWeight > this.simThresholdForPrediction) {
				weightSum += similarityWeight;
				// add up the differences of the user average
				ratingSum = (dataModel.getRating(user, otherItem) - userAvg) * similarityWeight;
//				System.out.println("existing rating was: " + dataModel.getRating(user, otherItem));
				existingRatingsSum += ratingSum;
				counter++;
			}
			if (nbNeighborsForPrediction > 0 && counter >= nbNeighborsForPrediction)  {
				break;
			}
		}
		int nbToDivide = Math.min(counter, similarities.keySet().size());
		
		if (nbToDivide == 0) {
//			System.out.println("No neighbors found with ratings ..");
			if (this.fallBackRec != null) {
				return this.fallBackRec.predictRating(user, item);
			}
			return Float.NaN;
		
		}
		
//		result = (float) existingRatingsSum / nbToDivide;
		float result = (float) existingRatingsSum / (float) weightSum;
		result += userAvg;
//        System.out.println("predicting " + result + " based on " + similarities.keySet().size() + " ratings (max : " + nbNeighborsForPrediction + "), nbTodivide:[ " + nbToDivide + "]");
		return result;
	}

	// =====================================================================================
	/**
	 * We take the user's profile and return the non-seen items ranked according to the
	 * cosine similarity of the vectors of the profile and the item
	 */
	@Override
	public List<Integer> recommendItems(int user) {
		List<Integer> result = new ArrayList<Integer>();
		
		// Get the profile
		Map<Integer, Double> userProfile = this.userProfiles.get(user);
		if (userProfile == null) {
			if (fallBack == null) {
				Debug.log("ContentBasedRecommender:recommend : NO PROFILE for user " + user + " - returning empty list");
				return result;
			}
			else {
				Debug.log("No profile - using fallback: "  + this.fallBack);
				return this.fallBackRec.recommendItems(user);
			}
//			return result;
		}
		
		// Prepare a list for the results
		Map<Integer, Double> similarities = new HashMap<Integer, Double>();
		// go through the items
		float rating = -1;
		double similarity = 0;
		for (Integer item : dataModel.getItems()) {
			similarity = 0.0;
			// check if we have a rating for it
			rating = dataModel.getRating(user, item);
			
			if (hideKnownItems && rating != -1) {
				//if hideKnownItems is true, we only want to recommend items which the user does not know
				//thus, if hideKnownItems is and the item is known (rating != -1), we make no prediction
				continue;
			}
			
			// Get the feature vector for the item
			Map<Integer,Double> featureVector = this.featureWeights.get(item);
			// Check if we have a feature vector for it
			if (featureVector != null) {
				similarity = Utilities101.cosineSimilarity(userProfile, featureVector, this.wordlist);
				if (Double.isNaN(similarity)) {
					similarity = 0.0;
				}
				similarities.put(item, similarity);
			}
			
		}
		// Once we have the similarities, we sort them in descending order and return them
		Map<Integer, Double> sortedMap = Utilities101.sortByValueDescending(similarities);
		
		result.addAll(sortedMap.keySet());
		
		
		// DEBUG:
//		System.out.println("User liked");
//		for (Integer item : likedItemsPerUser.get(user)) {
//			System.out.println(featureVectorAsString(featureWeights.get(item), this.wordlist));
//		}
//
//		System.out.println("User profile");
//		System.out.println(featureVectorAsString(this.userProfiles.get(user), wordlist));
//		System.out.println("Recommending: ");
//		for (int i=0;i<5;i++) {
//			System.out.println(result.get(i) + " " + similarities.get(result.get(i)));
//			System.out.println(featureVectorAsString(featureWeights.get(result.get(i)), this.wordlist));
//		}
		
		return result;
	}

	// =====================================================================================

	
	/**
	 * We load the content-information into memory and calculate the profile vectors 
	 */
	@Override
	public void init() throws Exception {

		// load the word list
		wordlist = Utilities101.loadwordlist(dataDirectory + "/" + wordListFile);
		// load feature vectors
		featureWeights = Utilities101.loadFeatureWeights(dataDirectory + "/" + featureWeightFile);
		// Get the liked items of the past of the user
		likedItemsPerUser = Utilities101.getPastLikedItemsOfUsers(dataModel);
		
		// load cosine similarities once
		if (cosineSimilarities == null) {
			
			// if the cosine similarities file does not exist, create it
			File filecheck = new File(dataDirectory + "/" + cosineSimilaritiesFile);
			if(!filecheck.exists()){
				Utilities101.createCosineSimilaritiesFile(dataDirectory, featureWeights, wordlist, 1);
			}
			
			cosineSimilarities = Utilities101.loadCosineSimilarities(dataDirectory + "/" + cosineSimilaritiesFile);
		}
		 
		// calculate user profiles from transactions
		userProfiles = loadUserProfiles();
		
		// Remember the user averages as default for the prediction
		userAverages = dataModel.getUserAverageRatings();
		
		// Remember the item averages
		itemAverages = Utilities101.getItemAverageRatings(dataModel.getRatings());
		
		if (fallBack != null) {
			if ("FunkSVD".equalsIgnoreCase(fallBack)) {
				this.fallBackRec = new FunkSVDRecommender();
			}
			else if ("SlopeOne".equalsIgnoreCase(fallBack)) {
				this.fallBackRec = new SlopeOneRecommender();
			}
			else if ("PopRank".equalsIgnoreCase(fallBack)) {
				this.fallBackRec = new PopularityAndAverage();
			}
			
			this.fallBackRec.setDataModel(getDataModel());
			this.fallBackRec.init();
		}


	}
	
	// =====================================================================================


	/**
	 * Calculate the profile vectors for each user based on his past liked items
	 * The calculation is based on the average profile vector
	 * @return
	 */
	@SuppressWarnings("unused")
	Map<Integer, Map<Integer, Double>> loadUserProfiles() {
		Map<Integer,Map<Integer, Double>> result = new HashMap<Integer, Map<Integer, Double>>();
		int cnt = 1;
		for (Integer user : dataModel.getUsers()) {
			// Get the positively rated items of the user
			Set<Integer> likedItems = likedItemsPerUser.get(user);
			// If we have no likedItems, we have to go to the next. 
			if (likedItems == null) {
				// cannot build a profile
				continue;
			}
			
//			System.out.println("Liked by user: " + user + " " + likedItems);

			List<Map<Integer, Double>> vectorsOfLikedItems = new ArrayList<Map<Integer, Double>>(); 
			for (Integer item : likedItems) {
//				System.out.println("User " + user.getID() + " liked: " + aritem);
				// Get the features weights
				Map<Integer, Double> weightvector = featureWeights.get(item);
				if (weightvector != null) {
					vectorsOfLikedItems.add(weightvector);
				}
				else {
					// Do nothing. There are not vectors for every product available
//				System.out.println("---> Missing vector for " + itemID);
				}
			}
			// Ok, we have the vectors. get the word list, sum up everything and divide by number of liked items
			if (vectorsOfLikedItems.size() > 0) {
				// create the profile vector entries from the wordlist. We need all the values here.
				Map<Integer, Double> profileVectorMap = new HashMap<Integer, Double>();
				// go through the word list and initialize the weights with zero
//				int i = 0;
//				for (String word : this.wordlist) {
//					profileVectorMap.put(i, 0.0);
//					i++;
//				}
				// go through all the item vectors
				for (Map<Integer, Double> weightvector : vectorsOfLikedItems) {
					// and copy things to the global vector. at the end, we will divide things by the number of items
					for (Integer wordidx : weightvector.keySet()) {
						Double oldvalue = profileVectorMap.get(wordidx);
						if (oldvalue == null) {
							profileVectorMap.put(wordidx, 0.0);
						}
						profileVectorMap.put(wordidx, profileVectorMap.get(wordidx) + weightvector.get(wordidx));
					}
				}
				// now we divide things..
				
				for (Integer wordidx : profileVectorMap.keySet()) {
					// get the value and divide it by the number of liked items
					profileVectorMap.put(wordidx, profileVectorMap.get(wordidx) / (double) likedItems.size());
				}
				result.put(user,profileVectorMap);
			}
			else {
				// set null (i.e. do nothing) 
			}
			
			// debug
			cnt++;
//			if (cnt >= 20) { break; }
			
		}
		return result;
	}

	// =====================================================================================

	/**
	 * A helper to print a feature vector with names
	 * @param features the list of features with their weights
	 * @param wordlist the word list
	 * @return A string representation of the feature vector
	 */
	static String featureVectorAsString(Map<Integer,Double> features, List<String> wordlist) {
		DecimalFormat df = new DecimalFormat("#.##");
		
		// sort by keyword id
//		List<Integer> sortedkeys = new ArrayList<Integer>(features.keySet());
//		Collections.sort(sortedkeys);

		Map<Integer,Double> sortedMap = Utilities101.sortByValueDescending(features);
		List<Integer> sortedkeys = new ArrayList<Integer>(sortedMap.keySet());
		
		String result = "Features (" + sortedkeys.size() + "): ";
		for (Integer key : sortedkeys) {
			result += wordlist.get(key);
			result += "(" + key + "[" + df.format(features.get(key)) + "]) ";
		}
//		result += "\n";
		return result;
	}

	// =====================================================================================

	/**
	 * Overwrite the data directory
	 * @param directory
	 */
	@R101Setting(displayName="Data directory", description="Overwrite the data directory",
			defaultValue="data/movielens/", type=SettingsType.TEXT)
	public void setDataDirectory(String directory) {
		dataDirectory = directory;
	}

	// =====================================================================================
	@R101Setting(defaultValue="10", type=SettingsType.INTEGER,
			displayName="Number of neighbors", description="Number of neighbors for prediction",
			minValue=0)
	public void setNbNeighborsForPrediction(String nb) {
		if (nb != null) {
			this.nbNeighborsForPrediction = Integer.parseInt(nb);
		}
	}
	
	// =====================================================================================
	
	public String toString(){
		return this.getConfigurationFileString();
//		return "Content-Based Recommender, nb = " + this.nbNeighborsForPrediction;
		
	}

	/**
	 * Returns the loaded feature weights 
	 * @return
	 */
	public Map<Integer, Map<Integer, Double>> getFeatureWeights() {
		return featureWeights;
	}
	
	/**
	 * Set the fallback method
	 * @param fb
	 */
	@R101Setting(displayName="Fallback", description="Set the fallback method",
			defaultValue="FunkSVD", type=SettingsType.ARRAY, values={"FunkSVD","SlopeOne","PopRank"})
	public void setFallBack(String fb) {
		fallBack = fb;
	}

	/**
	 * The minimum similarity value.
	 * @param sim
	 */
	@R101Setting(displayName="Minimum similarity", type=SettingsType.DOUBLE,
			description="The minimum similarity value", minValue=0, defaultValue="0")
	public void setMinSimilarityForPrediction(String sim) {
		this.simThresholdForPrediction = Double.parseDouble(sim);
	}
	

	@Override
	public int getDurationEstimate() {
		return 9;
	}


	/**
	 * Define the feature weight file: Default tf-idf-vectors.txt
	 * @param featureWeights
	 */
	@R101Setting(displayName="Feature weight file",
			description="Defines the feature weight file", defaultValue="tf-idf-vectors.txt",
			type=SettingsType.FILE)
	public void setFeatureWeightFile(String featureWeightFile) {
		this.featureWeightFile = featureWeightFile;
	}

	/**
	 * Overrides the wordlist file
	 * @param wordListFile
	 */
	@R101Setting(displayName="Word list file", type=SettingsType.FILE,
			description="Overrides the wordlist file", defaultValue="wordlist.txt")
	public void setWordListFile(String wordListFile) {
		this.wordListFile = wordListFile;
	}


	@Override
	protected void hideKnownItems(boolean value) {
		hideKnownItems = value;
	}
	
	
	
	
}
