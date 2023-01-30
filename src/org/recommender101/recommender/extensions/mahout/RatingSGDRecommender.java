package org.recommender101.recommender.extensions.mahout;

import org.recommender101.recommender.extensions.mahout.impl.Factorizer;
import org.recommender101.recommender.extensions.mahout.impl.RatingSGDFactorizer;
import org.recommender101.recommender.extensions.mahout.impl.AbstractSVDRecommender;
import org.recommender101.recommender.extensions.mahout.impl.exception.TasteException;

/** 
 * Ported from Apache Mahout. Original code is encapsulated in {@link RatingSGDFactorizer}.
 * 
 * Matrix factorization with user and item biases for rating prediction, trained with plain vanilla SGD 
 * 
 * @author MJ
 */
@SuppressWarnings("serial")
public class RatingSGDRecommender extends AbstractSVDRecommender {

	@Override
	public void init() throws TasteException {
		Factorizer fact = new RatingSGDFactorizer(dataModel, numFeatures, learningRate, preventOverfitting, randomNoise, numIterations, learningRateDecay);
		factorization = fact.factorize();
	}
}
