package org.recommender101.eval.metrics;

import java.util.List;

import org.recommender101.eval.interfaces.RecommendationlistEvaluator;
import org.recommender101.gui.annotations.R101Class;

/**
 * Calculates the average MRR (Mean Reciprocal Rank) value of the recommendation lists.
 * 
 * @author LL
 * 
 */
@R101Class(name="Mean Reciprocal Rank (MRR)", description="Calculates the average MRR (Mean Reciprocal Rank) value of the recommendation lists.")
public class MRR extends RecommendationlistEvaluator {

	float mrr = 0; 
	int count = 0;
	
	@Override
	public void addRecommendations(Integer user, List<Integer> list) {
		// go through the first topN items of the recommendation list (topN set by superclass)
		for (int i = 0; i < topN; i++) {
			// if the recommendation list is shorter than topN, just skip and record it as a miss
			if (i >= list.size()) {
			
			// position i exists in the list
			} else {
				// get the item at the i'th position
				int item = list.get(i);
				// if it's a hit (relevant)
				if (isItemRelevant(item, user)) {
					//add its position's rank to the mrr
					mrr += 1.0 / (i + 1); // we add +1 here since i's first position is 0
				}
			}
			// count the items that were looked at (this is basically = topN)
			count++;
		}
	}

	@Override
	public float getEvaluationResult() {
		// return the average of the rank values
		return mrr/count;
	}
}
