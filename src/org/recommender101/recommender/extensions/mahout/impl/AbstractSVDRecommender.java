package org.recommender101.recommender.extensions.mahout.impl;

import java.util.List;

import org.recommender101.recommender.AbstractRecommender;
import org.recommender101.recommender.extensions.mahout.impl.exception.NoSuchItemException;
import org.recommender101.recommender.extensions.mahout.impl.exception.NoSuchUserException;
import org.recommender101.recommender.extensions.mahout.impl.exception.TasteException;

/**
 * A base recommender class for all SVD-based recommender algorithm in mahout. 
 * Has some default config values, which are shared by all the sub-algorithms. 
 * Additionally implements the predict and rank methods.
 * @author MJ
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractSVDRecommender extends AbstractRecommender{

	protected Factorization factorization;
	
	protected int numFeatures = 50;
	protected double learningRate = 0.01;
	protected double preventOverfitting = 0.1;
    protected double randomNoise = 0.01;
    protected int numIterations = 50;
    protected double learningRateDecay = 1.0;
	
	@Override
	public float predictRating(int user, int item) {
		if(factorization==null){
			throw new RuntimeException("The superclass has not set the Factorizer instance. 'You had one job ...'");
		}
		try {
			double[] userFeatures = factorization.getUserFeatures(user);
		    double[] itemFeatures = factorization.getItemFeatures(item);
		    double estimate = 0;
		    for (int feature = 0; feature < userFeatures.length; feature++) {
		      estimate += userFeatures[feature] * itemFeatures[feature];
		    }
		    return (float) estimate;
		} catch (NoSuchItemException | NoSuchUserException e) {
			return Float.NaN;
		}
	}

	@Override
	public List<Integer> recommendItems(int user) {
		return recommendItemsByRatingPrediction(user);
	}

	@Override
	public abstract void init() throws TasteException;
	
	/**
	 * @param numFeatures the numFeatures to set
	 */
	public void setNumFeatures(String numFeatures) {
		this.numFeatures = Integer.parseInt(numFeatures);
	}

	/**
	 * @param learningRate the learningRate to set
	 */
	public void setLearningRate(String learningRate) {
		this.learningRate = Double.parseDouble(learningRate);
	}

	/**
	 * @param preventOverfitting the preventOverfitting to set
	 */
	public void setPreventOverfitting(String preventOverfitting) {
		this.preventOverfitting = Double.parseDouble(preventOverfitting);
	}

	/**
	 * @param randomNoise the randomNoise to set
	 */
	public void setRandomNoise(String randomNoise) {
		this.randomNoise = Double.parseDouble(randomNoise);
	}

	/**
	 * @param numIterations the numIterations to set
	 */
	public void setNumIterations(String numIterations) {
		this.numIterations = Integer.parseInt(numIterations);
	}

	/**
	 * @param learningRateDecay the learningRateDecay to set
	 */
	public void setLearningRateDecay(String learningRateDecay) {
		this.learningRateDecay = Double.parseDouble(learningRateDecay);
	}
}
