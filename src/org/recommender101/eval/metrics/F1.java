package org.recommender101.eval.metrics;

import java.util.List;

import org.recommender101.eval.interfaces.RecommendationlistEvaluator;
import org.recommender101.gui.annotations.R101Class;
import org.recommender101.recommender.AbstractRecommender;

/**
 * Implements variants of the F1 method
 * @author LL
 *
 */
@R101Class(name="F1", description="Implements variants of the F1 method")
public class F1 extends RecommendationlistEvaluator {


	Precision precision;
	Recall recall;
	
	/**
	 * Determines the F1 value
	 */
	@Override
	public void addRecommendations(Integer user, List<Integer> list) {
		precision.addRecommendations(user, list);
		recall.addRecommendations(user, list);
	}
	
	/**
	 * Create and initialize internal precision and recall
	 */
	@Override
	public void initialize() {
		//Create an internal precision and recall
		
		//first create a class and set the parameter that the ClassInstatiator would set
		precision = new Precision();
		precision.setTopN(String.valueOf(this.getTopN()));
		precision.setTargetSet(this.getTargetSet());
		//then set the parameters that Experiment would set
		precision.setTrainingDataModel(this.getTrainingDataModel());
		precision.setTestDataModel(this.getTestDataModel());
		precision.initialize();
		
		//do the same for the recall
		recall = new Recall();
		recall.setTopN(String.valueOf(this.getTopN()));
		recall.setTargetSet(this.getTargetSet());
		recall.setTrainingDataModel(this.getTrainingDataModel());
		recall.setTestDataModel(this.getTestDataModel());
		recall.initialize();
	}
	
	/**
	 * Override the setRecommenderName method since the internal metrics need this property
	 */
	@Override
	public void setRecommenderName(String recommenderName) {
		super.setRecommenderName(recommenderName);
		precision.setRecommenderName(this.getRecommenderName());
		recall.setRecommenderName(this.getRecommenderName());
	}
	
	/**
	 * Override the setRecommender method since the internal recommender need this property
	 */
	@Override
	public void setRecommender(AbstractRecommender recommender) {
		super.setRecommender(recommender);
		precision.setRecommender(this.getRecommender());
		recall.setRecommender(this.getRecommender());
	}

	/**
	 * Calculate the result from the internal metrics
	 */
	@Override
	public float getEvaluationResult() {
		float p = precision.getEvaluationResult();
		float r = recall.getEvaluationResult();
		
		float f1;
		if (p+r == 0) f1 = 0;
		else f1 = 2*(p*r)/(p+r);
		
		return f1;
	}
}
