/** DJ **/
package org.recommender101.eval.metrics;

import org.recommender101.data.Rating;
import org.recommender101.eval.interfaces.PredictionEvaluator;
import org.recommender101.gui.annotations.R101Class;

/**
 * Calculates the MAE
 * @author DJ
 *
 */
@R101Class(name="Mean Absolute Error (MAE)", description="Calculates the Mean Absolute Error.")
public class MAE extends PredictionEvaluator {

	/**
	 * We calculate the errors
	 */
	float errorAccumulator;
	
	/**
	 * The number of predictions
	 */
	int predictionCount;
	
	/**
	 * If this parameter is set, we report the number of predictions where 
	 * the true rating was at least countBadRecommendations lower than the true rating
	 */
	double countBadRecommendations = -1;
	
	// =====================================================================================

	@Override
	/**
	 * Record a new prediction and determine the deviation from the true value
	 */
	public void addTestPrediction(Rating r, float prediction) {
		if (!Float.isNaN(prediction)) {
			
			Float rating = r.rating;
			if (rating != null) {
				float error = Math.abs(rating - prediction);
				// Default behavior. Measure the error
				if (countBadRecommendations == -1) {
					errorAccumulator += error;
				}
				else {
					// Count the BIG errors
					if (error > countBadRecommendations) {
						errorAccumulator++;
					}
				}
				predictionCount++;
			}
		}
		
	}

	// =====================================================================================

	/**
	 * Return the mean error at the end of the process
	 */
	@Override
	public float getPredictionAccuracy() {
		return errorAccumulator / (float) predictionCount;
	}

	/**
	 * String rep for eval.
	 */
	public String toString() {
		return "MAE";
	}

	/**
	 * Get the parameter value
	 * @return the value
	 */
	public double getCountBadRecommendations() {
		return countBadRecommendations;
	}

	/*
	 * Set the parameter value
	 * @param countBadRecommendations
	 */
	public void setCountBadRecommendations(String countBadRecommendations) {
		try {
			this.countBadRecommendations = Double.parseDouble(countBadRecommendations);
		}
		catch (Exception e) {
			System.err.println("MAE countBadRecommendations parameter must be a positive double");
		}
	}

	
}
