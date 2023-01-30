package org.recommender101.eval.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.recommender101.data.Rating;
import org.recommender101.eval.interfaces.RecommendationlistEvaluator;
import org.recommender101.gui.annotations.R101Class;

/**
 * Calculates the nDCG (normalized discounted cumulative gain) value.
 * There are two variants: allintestset and allrelevantintestset.
 * The first one assigns non-rated but recommended items a relevance of 0.
 * The second one ignores all non-rated items.
 * 
 * @author timkraemer, LL
 * 
 */
@R101Class(name="Normalized DCG (nDCG)", description="Calculates the nDCG (normalized discounted cumulative gain).")
public class NDCG extends RecommendationlistEvaluator {

	/** Stores the type of the target set */
	evalTypes targetSetType = evalTypes.allrelevantintestset;

	// The execution modes
	public enum evalTypes {
		allrelevantintestset, allintestset
	};
	
	private double accumulatedNDCGValue = 0.0;
	private int count = 0;
	
	
	@Override
	public void initialize() {
		String targetSet = getTargetSet();
		if (targetSet != null) {
			targetSetType = evalTypes.valueOf(targetSet.toLowerCase());
		}
	}
	
	@Override
	public void addRecommendations(Integer user, List<Integer> list) {
		int depth = Math.min(getTopN(), list.size());
		
		// Calculate the DCG value
		double dcg = 0.0;
		int loopCount = 0;
		for (int item:list)
		{
			if (targetSetType == NDCG.evalTypes.allrelevantintestset){
				// if user has not rated the item, ignore it for NDCG
				if (getTestDataModel().getRating(user, item) != -1) {
					double a = Math.pow(2, getTestDataModel().getRating(user, item));
					// I've used "2+i" below instead of "1+i" because the index is
					// zero-based.
					double b = Math.log(2 + loopCount) / Math.log(2);
					//System.out.println("a: "+a+", b: "+b+", a/b: "+a/b);
					loopCount++;				
					dcg += a / b;
				}
				
				
			}
			else{
				// if user has not rated item, it has no relevance (-> lowers NDCG)
				double a;
				if (getTestDataModel().getRating(user, item) == -1) a = 0;
				else a = Math.pow(2, getTestDataModel().getRating(user, item));
				// I've used "2+i" below instead of "1+i" because the index is
				// zero-based.
				double b = Math.log(2 + loopCount) / Math.log(2);
				System.out.println("a: "+a+", b: "+b+", a/b: "+a/b);
				loopCount++;				
				dcg += a / b;
				
				
			}
			if (loopCount >= depth)
				break;
		}
		
		// Get the List of ratings and sort them by relevance (rating)
		ArrayList<Rating> ratings = new ArrayList<Rating>(getTestDataModel().getRatingsOfUser(user));
		Collections.sort(ratings, new Comparator<Rating>() {

			@Override
			public int compare(Rating o1, Rating o2) {
				return Float.compare(o2.rating, o1.rating);
			}

		});

		// Calculate "ideal DCG"
		double ideal_dcg = 0.0;
		// if only all relevant items in testset should be considered,
		//  then the i_dcg could have a smaller depth (same as dcg)
		if(targetSetType == NDCG.evalTypes.allrelevantintestset) depth = loopCount;

		for (int i = 0; i < depth; i++) {
			double a = Math.pow(2,ratings.get(i).rating);
			// I've used "2+i" below instead of "1+i" because the index is
			// zero-based.
			double b = Math.log(2 + i) / Math.log(2);
			//System.out.println("a2: "+a+", b2: "+b+", a/b: "+a/b);
			ideal_dcg += a / b;
		}

		double nDCG = dcg/ideal_dcg;
		
		//System.out.println("DCG: " + dcg + ", ideal DCG: " + ideal_dcg+", nDCG: "+nDCG);
		
		// DJ: check for Double.NaN
		if (!Double.isNaN(nDCG)) {
			
			count++;
			accumulatedNDCGValue += nDCG;
		}
	}

	@Override
	public float getEvaluationResult() {
		return ((float)accumulatedNDCGValue)/((float)count);
	}

}
