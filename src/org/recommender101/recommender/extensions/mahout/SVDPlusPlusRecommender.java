package org.recommender101.recommender.extensions.mahout;

import org.recommender101.recommender.extensions.asymmetricsvd.AsymmetricSVDRecommender;
import org.recommender101.recommender.extensions.funksvd.FunkSVDRecommender;
import org.recommender101.recommender.extensions.mahout.impl.Factorizer;
import org.recommender101.recommender.extensions.mahout.impl.SVDPlusPlusFactorizer;
import org.recommender101.recommender.extensions.mahout.impl.AbstractSVDRecommender;
import org.recommender101.recommender.extensions.mahout.impl.exception.TasteException;

/**
 * Ported from Apache Mahout. Original code is encapsulated in {@link SVDPlusPlusFactorizer}.
 * 
 * NOTE: This implementation is very slow! It is inefficiently programmed. 
 * Take a look at {@link AsymmetricSVDRecommender} for how it should be done.
 * 
 * SVD++, an enhancement of classical matrix factorization for rating prediction.
 * Additionally to using ratings (how did people rate?) for learning, this model also takes into account
 * who rated what.
 *
 * Yehuda Koren: Factorization Meets the Neighborhood: a Multifaceted Collaborative Filtering Model, KDD 2008.
 * http://research.yahoo.com/files/kdd08koren.pdf
 * 
 * Note: This version of SVD++ is probably not implemented the way Koren intended it to be. 
 * The iteration scheme is (equivalent to {@link FunkSVDRecommender} or {@link RatingSGDRecommender}) randomized,
 * instead of user-wise like described in "Factor in the Neighbors: Scalable and
 * Accurate Collaborative Filtering" (http://courses.ischool.berkeley.edu/i290-dm/s11/SECURE/a1-koren.pdf).
 * This way, many parts of the rating prediction can be calculated in common and the execution is much faster.
 * Because this is not the case in the implementation, its not advised to use it when time is of the essence 
 * (e.g. it takes 2 hours for one cross-validation run on the movielens 1m set on a fast PC).
 * 
 * @author MJ
 */
@SuppressWarnings("serial")
public class SVDPlusPlusRecommender extends AbstractSVDRecommender{

	/**
	 * Modify default parameter values slightly
	 */
	public SVDPlusPlusRecommender(){
		learningRate = 0.002;
		preventOverfitting = 0.04;
	}
	
	@Override
	public void init() throws TasteException {
		Factorizer fact = new SVDPlusPlusFactorizer(dataModel, numFeatures, learningRate, preventOverfitting,
			      randomNoise, numIterations, learningRateDecay);
		factorization = fact.factorize();
	}
}
