/** DJ **/
package org.recommender101.recommender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.recommender101.data.DataModel;
import org.recommender101.data.Rating;
import org.recommender101.eval.impl.Recommender101Impl;
import org.recommender101.tools.Instantiable;
import org.recommender101.tools.Utilities101;

/**
 * The core recommender interface
 * @author DJ
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractRecommender extends Instantiable implements Comparable<AbstractRecommender>, Serializable {

	/**
	 * A handle to the data model
	 */
	protected DataModel dataModel;
	
	/**
	 * A map pointing to arbitrary additional objects 
	 */
	
	protected Map<String,Object> extraInformation = new HashMap<String, Object>();
	
	
  // =====================================================================================
  /**
	 * Predicts a rating for the user
	 * @param user
	 * @param item
	 * @return the rating value
	 */
	public abstract float predictRating(int user, int item);
	
	/**
	 * Generates a ranked list of recommendations 
	 * @param user
	 * @return the ranked list of items (in descending order)
	 */
	 public abstract List<Integer> recommendItems(int user);
	
	/**
	 * A general method for ranking items according to their rating prediction.
	 * The method should be overwritten in case the recommender cannot make 
	 * rating predictions or when a better heuristic is needed, which for 
	 * example takes the popularity of the recommendations into account.
	 * @param user the user for which a recommendation is sought
	 * @return the ranked list of items
	 */
	public List<Integer> recommendItemsByRatingPrediction(int user) {
		List<Integer> result = new ArrayList<Integer>();

		// If there are no ratings for the user in the test set,
		// there is no point of making a recommendation.
		Map<Integer, Set<Rating>> ratings = getDataModel().getRatingsPerUser();
		// If we have no ratings...
		if (ratings == null || ratings.size() == 0) {
			return Collections.emptyList();
		}

		// Calculate rating predictions for all items we know
		Map<Integer, Float> predictions = new HashMap<Integer, Float>();
		//byte rating = -1;
		float pred = Float.NaN;
		// Go through all the items
		for (Integer item : dataModel.getItems()) {
			boolean userHasAlreadyRatedItem = false;
			// We will not recommend items repeatedly here
			float rating = dataModel.getRating(user, item);
			if (rating != -1) {
				userHasAlreadyRatedItem = true;
			}
			
			if (!userHasAlreadyRatedItem) {
				// make a prediction and remember it in case the recommender
				// could make one
				pred = predictRating(user, item);
				if (!Float.isNaN(pred)) {
					predictions.put(item, pred);
				}
			}
		}
		
		predictions = filterElementsByRelevanceThreshold(predictions, user);
		predictions = Utilities101.sortByValueDescending(predictions);
		
		for (Integer item : predictions.keySet()) {
			result.add(item);
		}
		return result;
	}
	
	
	
	/**
	 * A method that removes all elements whose prediction value is below the relevance threshold.
	 */
	public Map<Integer, Float> filterElementsByRelevanceThreshold(Map<Integer, Float> predictions, int user) {

		// Do nothing if the filter is not set
		if (Recommender101Impl.FILTER_NON_RELEVANT_ITEMS_FOR_RECOMMENDATION) {
			Map<Integer, Float> result = new HashMap<Integer, Float>();
			
			// Look what we should do
			boolean ratingThreshold = true;
			
			Map<Integer, Float> userAverages = null;
			
			if (Recommender101Impl.PREDICTION_RELEVANCE_MIN_RATING_FOR_RELEVANCE == -1) {
				ratingThreshold = false;
				userAverages = getDataModel().getUserAverageRatings();
			}
			
			// go through the items
			for (Integer item : predictions.keySet()) {
				Float prediction = predictions.get(item);
				if (prediction != null) {
					if (ratingThreshold) {
						if (prediction > Recommender101Impl.PREDICTION_RELEVANCE_MIN_RATING_FOR_RELEVANCE) {
							result.put(item,prediction);
						}
					}
					else  {
						Float minRating = userAverages.get(user);
						if (minRating != null) {
							double factor = Recommender101Impl.PREDICTION_RELEVANCE_MIN_RATING_FOR_RELEVANCE * 0.01;
							minRating = (float) (minRating * (1 + factor));
							if (prediction > minRating) {
								result.put(item,prediction);
							}
						}
						
					}
				}
			}
			return result;
		}
		else {
			return predictions;
		}
	}
	

	/**
	 * Sets the data model and do all initializations here
	 * @param dm
	 */
	public void setDataModel(DataModel dm) {
		dataModel = dm;
	};
	
	/**
	 * Returns the data model
	 * @return
	 */
	public DataModel getDataModel() {
		return dataModel;
	}

	
	/**
	 * Returns the estimated computation time of the algorithm.
	 * Numbers should be between 1 to 10 where 10 indicates the 
	 * longest running times, e.g., for user-user kNN
	 * 
	 * Value should be overwritten in recommender implementations (init-method) or set
	 * via the property file
	 * 
	 * * @return 5 as a default in the abstract class
	 * 
	 */
	public int getDurationEstimate() {
		return 5;
	}
	
	
	
	/**
	 * Returns the extra information object
	 * 
	 */
	public Map<String, Object> getExtraInformationMap() {
		return extraInformation;
	}

	/**
	 * Sets the extra information map
	 * @param extraInformation
	 */
	public void setExtraInformatioMapn(Map<String, Object> extraInformation) {
		this.extraInformation = extraInformation;
	}

	/**
	 * Set an individual field of the extra information map
	 * @param key
	 * @param value
	 */
	public void setExtraInformation(String key, Object value) {
		this.extraInformation.put(key, value);
	}
	
	/**
	 * Return the extra information for a given key
	 * @param key the key under which the information is stored
	 * @return the object or null
	 */
	public Object getExtraInformation(String key) {
		return this.extraInformation.get(key);
	}
	
	
	/**
	 * An init method which will be called by the instantiating class after object creation
	 * @param dm the data model to be used
	 */
	public abstract void init() throws Exception;
	
	/**
	 * Default: Return the configuration file string without the leading package name
	 */
	@Override
	public String toString() {
		String result = this.getConfigurationFileString();
		if (result != null) {
			result = Utilities101.removePackageQualifiers(result);
			return result;
		}
		else {
			return "No algorithm configuration provided";
		}
	} 
	
	/**
	 * Sort recommenders according to their running times
	 */
	@Override
	public int compareTo(AbstractRecommender r1) {
		return r1.getDurationEstimate() - this.getDurationEstimate();
	}
	
	/**
	 * Determines if this instance of the recommender algorithm shall return items that the user already knows.
	 * E.g. in the movie domain, if this parameter is set to true, the recommender will not recommend movies 
	 * that the user has explicitly liked or rated in the training set. If the parameter is set to false, the recommender 
	 * can potentially recommend all items from the item space including items that the user already knows.
	 * @param value
	 */         
	public void setHideKnownItems(String value){
		hideKnownItems(Boolean.parseBoolean(value));
	}

	/**
	 * See {@link AbstractRecommender#setRecommendKnownItems(String)}. A recommender has to override this method,
	 * if the parameter is configurable in the respective algorithm. If not, and if the users tries to set the parameter,
	 * they will get an exception telling them that the algorithm cannot be configured to either recommend or not recommend
	 * known items and the they shall refrain from setting the parameter because the behavior will be indeterminate either way.
	 * @param parseBoolean
	 */
	protected void hideKnownItems(boolean value) {
		throw new UnsupportedOperationException("This algorithm does not support the 'recommend known items' parameter. "
				+ "If you want to be sure of the algorithm's behavior, you need to check its implementation");
	}

	
}
