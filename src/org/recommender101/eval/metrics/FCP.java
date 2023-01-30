package org.recommender101.eval.metrics;

import java.util.ArrayList;
import java.util.List;

import org.recommender101.data.Rating;
import org.recommender101.eval.interfaces.RecommendationlistEvaluator;
import org.recommender101.gui.annotations.R101Class;

/**
 * Implements variants of the Fraction of Concordant Pairs metric
 * 
 * FCP(D,y^) := SUM{(u,i,y),(u,i',y') in D}( d(y>y') * d( y^(u,i) > y^(u,i') ) )  /  SUM{(u,i,y),(u,i',y') in D}( d(y>y') )
 * with d(A) = 1 if A = true, else 0
 * 
 * This metric measures the fraction of all correct-ordered pairs in the recommendation list, compared with the test set.
 * For the testset, the order is determined by the rating. Therefore, this metric only works if the testset has different ratings.
 * Non-rated items (rating = -1) are ignored, therefore this metric does not work for implicit testsets that have only 1s as ratings.
 * 
 * If there is a special testset, that is e.g. ordered by time, number of clicks, etc., this must be implemented additionally.
 * 
 * @author LL
 *
 */
@R101Class(name="FCP", description="Implements variants of the Fraction of Concordant Pairs metric.")
public class FCP extends RecommendationlistEvaluator {

	// the final division
	int top = 0;
	int bottom = 0;
	
	@Override
	public void addRecommendations(Integer user, List<Integer> list) {
		// return for a list with 0 or 1 ratings, since there have to be at least 2 to compare
		if (list.size() <= 1) return;
		
		
		List<Rating> listRatingsOfUser = new ArrayList<Rating>(getTestDataModel().getRatingsPerUser().get(user));
		// return for a list with 0 or 1 ratings, since there have to be at least 2 to compare
		if(listRatingsOfUser.size() <= 1) return;
		
		// run through all pairs of ratings
		for (int i = 0; i < listRatingsOfUser.size()-1; i++) {
			for (int j = i+1; j < listRatingsOfUser.size() ; j++){
				Rating yi = listRatingsOfUser.get(i);
				Rating yj = listRatingsOfUser.get(j);

				// do nothing when the rating is equal
				if (yi.rating == yj.rating) continue;
				// swap ratings
				else if (yi.rating < yj.rating){
					yi = listRatingsOfUser.get(j);
					yj = listRatingsOfUser.get(i);
				}
				// now yi.rating > yj.rating holds, so count the pair
				bottom++;
				int ydi = list.indexOf(yi.item);
				int ydj = list.indexOf(yj.item);
				
				// there are two cases where the item pairs in the recommendation list have to be counted:
				
				// 1) check whether item i comes before j in the recommendation list
				//   and if i's position is not -1 (not in the list).
				if (ydi < ydj && ydi != -1) top++;
				// 2) otherwise if j is before i but in truth j is just not in the list (aka -1)
				else if (ydj < ydi && ydj == -1) top++;		
			}
		}
	}

	@Override
	public float getEvaluationResult() {
		return (float)top/(float)bottom;
	}
	
	
}