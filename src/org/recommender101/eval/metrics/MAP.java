package org.recommender101.eval.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.recommender101.data.Rating;
import org.recommender101.eval.interfaces.RecommendationlistEvaluator;
import org.recommender101.gui.annotations.R101Class;

/**
 * Implements variants of the mean average precision @ n metric
 * 
 * MAP@n = SUM{i=1..N}(ap@n_i) / N
 * ap@n = SUM{k=1..n}(precision@k) / min(m,n)
 * N: #useres
 * m: #of relevant items
 * n: topN
 * 
 * @author LL
 *
 */
@R101Class(name="MAP", description="Implements variants of the mean average precision @ n metric.")
public class MAP extends RecommendationlistEvaluator {
	
	// TODO to remove a bit of redundancy, this class could be made a subclass of PrecisionRecall
	// however, this would make the implementation a bit less easy to read
	
	// global attribues of the MAP
	int N = 0;
	float sumOfAPs = 0;
	
	// evaluation mode
	boolean considerOnlyItemsRatedByUser = false;
	
	/** Stores the type of the target set */
	evalTypes targetSetType = evalTypes.allintestset;

	// The execution modes
	public enum evalTypes {
		allrelevantintestset, allintestset
	};
	
	@Override
	public void initialize() {
		String targetSet = getTargetSet();
		if (targetSet != null) {
			targetSetType = evalTypes.valueOf(targetSet.toLowerCase());
		}
		if (targetSetType == evalTypes.allrelevantintestset) {
			considerOnlyItemsRatedByUser = true;
		}
	}
	
	@Override
	public void addRecommendations(Integer user, List<Integer> list) {
		
		// return for empty list
		if (list.size() == 0) return;
		
		Set<Rating> ratingsOfUser = getTestDataModel().getRatingsPerUser().get(user);
		// get all the relevant items from the ratings of the user
		int relevantItems = 0;
		for (Rating r : ratingsOfUser) {
			// if this is a hit, we count it
			if (isItemRelevant(r.item, r.user)) {
				relevantItems++;
			}
		}
		// Cannot measure precision, no relevant items
		if (relevantItems == 0) return;
		
		float rating = -1;
		int recCounter = 0;
		int hitCounter = 0;
		List<Integer> finalRecommendation = new ArrayList<Integer>();
		float sumOfPrecisions = 0;
		//iterate the recommendations for k=1..n
		for (Integer item: list) {
			// get the rating of the item in the test set, -1 if not rated
			rating = getTestDataModel().getRating(user, item);
			
			// either consider every item or consider just the ones with a rating
			if (!considerOnlyItemsRatedByUser || rating != -1) {
				// Add the recommendation
				recCounter++;
				finalRecommendation.add(item);
				// if this is a hit, we count it
				if (isItemRelevant(item, user)) {
					hitCounter++;
				}
			}
			// in case not every item was considered
			else continue; // skip the item, eventually hop to the next rated item in the list							
			
			float precisionAtK;
			// this should never happen because empty list and no relevant items are handled before, but to be safe:
			if (recCounter == 0) precisionAtK = 0;
			else precisionAtK = hitCounter / (float) recCounter;				
			sumOfPrecisions += precisionAtK;
			
			//stop after topN items (aka: for k=1..n)
			if (recCounter >= topN)break;
		}
		
		// ap@n = SUM{k=1..n}(precision@k) / min(m,n)
		float apAtN = sumOfPrecisions / Math.min(relevantItems, topN);
		// add it to the sum of "SUM{i=1..N}(ap@n_i)" of MAP@n
		sumOfAPs += apAtN;
		// count the number of users
		N++;
	}

	/**
	 * Returns the average of all the ap@n values that are summed um in sumOfAPs.
	 * @return
	 */
	@Override
	public float getEvaluationResult() {
		return sumOfAPs / N; // MAP@n = SUM{i=1..n}(ap@n_i) / N
	}
}