package org.recommender101.eval.metrics;

import java.util.ArrayList;
import java.util.List;

import org.recommender101.data.Rating;
import org.recommender101.eval.interfaces.RecommendationlistEvaluator;
import org.recommender101.gui.annotations.R101Class;

/**
 * This metric creates the ROC (Receiver Operating Characteristic) curve for each user and calculates the
 * corresponding AUC value (area under curve). It returns the arithmetic mean of
 * all computed AUCs.
 * 
 * @author timkraemer
 * 
 */
@R101Class(name="ROC AUC", description="This metric creates the ROC (Receiver Operating Characteristic) curve for each user and calculates the corresponding AUC value (area under curve). It returns the arithmetic mean of all computed AUCs.")
public class ROCAUC extends RecommendationlistEvaluator {

	/**
	 * A private attribute which holds the number of users for which the AUC has
	 * been calculated
	 */
	private int avgCounter;

	/**
	 * The arithmetic mean of all previously calculated AUC values.
	 */
	private double aucAvg;

	/**
	 * Initialize the metric
	 */
	@Override
	public void initialize() {
		this.avgCounter = 0;
		this.aucAvg = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addRecommendations(Integer user, List<Integer> list) {
		// Check if list is null or empty
		if (list == null || list.size() == 0)
			return;

//		Debug.log("ROCAUC: AddRecommendations called (user: " + user + ")");

		int relevantItemsInTestSet = 0;
		int nonRelevantItemsInTestSet = 0;

		List<Integer> relevantList = new ArrayList<Integer>();
		for (Rating r : getTestDataModel().getRatingsPerUser().get(user)) {

			if (isItemRelevant(r.item, r.user)) {
				relevantItemsInTestSet++;
				relevantList.add(r.item);
			} else
				nonRelevantItemsInTestSet++;
		}

		if (relevantItemsInTestSet == 0 || nonRelevantItemsInTestSet == 0)
			return;

		/*System.out.println("relevant: " + relevantItemsInTestSet
				+ ", non relevant: " + nonRelevantItemsInTestSet);*/

		double stepSizeX = 1.0 / (double) nonRelevantItemsInTestSet;
		double stepSizeY = 1.0 / (double) relevantItemsInTestSet;

		double lastCoords[] = new double[] { 0.0, 0.0 };
		double auc = 0;


		for (int currItem : list) {
			float rating = getTestDataModel().getRating(user, currItem);

			if (rating == -1) {
				// No rating in test set found, discard
			} else {

				if (relevantList.contains(currItem)) {
					// Correct prediction
					lastCoords[1] += stepSizeY;
				} else {
					// Wrong predicition
					auc += stepSizeX * lastCoords[1];
					lastCoords[0] += stepSizeX;
				}

				// Print coordinates of the ROC curve. This should only be
				// commented in for debugging purposes.
				/*
				 * System.out.println(roundDouble(lastCoords[0]) + ";" +
				 * roundDouble(lastCoords[1]));
				 */
			}

		}

		this.aucAvg = (this.aucAvg * this.avgCounter + auc)
				/ (++this.avgCounter);
	}

	/**
	 * Returns the arithmetic mean of all calculated AUC values.
	 */
	@Override
	public float getEvaluationResult() {

		return (float) this.aucAvg;
	}

}
