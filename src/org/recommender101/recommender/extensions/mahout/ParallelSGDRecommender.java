package org.recommender101.recommender.extensions.mahout;

import org.recommender101.recommender.extensions.mahout.impl.Factorizer;
import org.recommender101.recommender.extensions.mahout.impl.ParallelSGDFactorizer;
import org.recommender101.recommender.extensions.mahout.impl.AbstractSVDRecommender;
import org.recommender101.recommender.extensions.mahout.impl.exception.TasteException;

/**
 * Ported from Apache Mahout. Original code is encapsulated in {@link ParallelSGDFactorizer}.
 * 
 * Minimalistic implementation of Parallel SGD factorizer based on
 * <a href="http://www.sze.hu/~gtakacs/download/jmlr_2009.pdf">
 * "Scalable Collaborative Filtering Approaches for Large Recommender Systems"</a>
 * and
 * <a href="hwww.cs.wisc.edu/~brecht/papers/hogwildTR.pdf">
 * "Hogwild!: A Lock-Free Approach to Parallelizing Stochastic Gradient Descent"</a> 
 * 
 * @author MJ
 * 
 * */
@SuppressWarnings("serial")
public class ParallelSGDRecommender extends AbstractSVDRecommender {

	private double biasMuRatio = 0.5;
	private double biasLambdaRatio = 0.1;
	private double forgettingExponent = 0;
	private int stepOffset = 0;
	private int numThreads = Runtime.getRuntime().availableProcessors();
	
	@Override
	public void init() throws TasteException {
		Factorizer fact = new ParallelSGDFactorizer(dataModel, numFeatures, preventOverfitting, numIterations,
			      learningRate, learningRateDecay, stepOffset, forgettingExponent,
			      biasMuRatio, biasLambdaRatio, numThreads);
		factorization = fact.factorize();
	}
	
	/**
	 * @param biasMuRatio the biasMuRatio to set
	 */
	public void setBiasMuRatio(String biasMuRatio) {
		this.biasMuRatio = Double.parseDouble(biasMuRatio);
	}

	/**
	 * @param biasLambdaRatio the biasLambdaRatio to set
	 */
	public void setBiasLambdaRatio(String biasLambdaRatio) {
		this.biasLambdaRatio = Double.parseDouble(biasLambdaRatio);
	}

	/**
	 * @param forgettingExponent the forgettingExponent to set
	 */
	public void setForgettingExponent(String forgettingExponent) {
		this.forgettingExponent = Double.parseDouble(forgettingExponent);
	}

	/**
	 * @param stepOffset the stepOffset to set
	 */
	public void setStepOffset(String stepOffset) {
		this.stepOffset = Integer.parseInt(stepOffset);
	}

	/**
	 * @param numThreads the numThreads to set
	 */
	public void setNumThreads(String numThreads) {
		this.numThreads = Integer.parseInt(numThreads);
	}

}
