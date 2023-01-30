package org.recommender101.recommender.extensions.asymmetricsvd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.recommender101.data.Rating;
import org.recommender101.gui.annotations.R101Class;
import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;
import org.recommender101.recommender.AbstractRecommender;
import org.recommender101.recommender.extensions.mahout.impl.exception.TasteException;

/**
 * A class that implements Koren's factorized neighborhood algorithm (item-item
 * version, also called Asymmetric-SVD)
 * 
 * Y. Koren: Factor in the Neighbors: Scalable and Accurate Collaborative Filtering (formula 13)
 * 
 * equivalent to:
 * Y. Koren: Factorization Meets the Neighborhood: a Multifaceted Collaborative Filtering Model (formula 13, called Asymmetric-SVD)
 * 
 * @author Zeynep, Dietmar, LL
 * 
 */
@SuppressWarnings("serial")
@R101Class(name="Asymmetic SVD Recommender", description="A class that implements Koren's factorized neighborhood algorithm (item-item), also called Asymmetric SVD")
public class AsymmetricSVDRecommender extends AbstractRecommender {
	
	// Algorithm parameters
	protected int iterations = 100; // 20 in Koren's paper
	protected double gammaStepSize = 0.002; // 0.002 in Koren's paper
	protected double lambdaForRegulation = 0.04; // 0.04 in Koren's paper
	protected int nbFactors = 100;
	protected double regBi = 25;
	protected double regBu = 10;

	// some defaults
	protected double lowerBound = -0.00000000001;
	protected double rangeOfRandomValues = 0.01;

	/** Average of Preferences. */
	protected double mAvgOfAllPreferences;

	/** Deviations (bu) after learning the Parameter by gradient solver. */
	protected Map<Integer, Double> mapOfBu = new HashMap<Integer, Double>();

	/** Deviations (bi) after learning the Parameter by gradient solver. */
	protected Map<Integer, Double> mapOfBi = new HashMap<Integer, Double>();

	/** Every User has an Array pu containing the factors */
	protected Map<Integer, double[]> mapOfPrecalculatedPuArrays = new HashMap<Integer, double[]>();
	
	/** Every Item has an Array qi containing the factors */
	protected Map<Integer, double[]> mapOfQiArrays = new HashMap<Integer, double[]>();

	/** Every Item has an Array xi containing the factors */
	protected Map<Integer, double[]> mapOfXiArrays = new HashMap<Integer, double[]>();

	/** Every Item has an Array yi containing the factors */
	protected Map<Integer, double[]> mapOfYiArrays = new HashMap<Integer, double[]>();

	/** Pre-calculate the preferences for each item */
	protected Map<Integer, Set<Rating>> ratingsOfItems = new HashMap<Integer, Set<Rating>>();
	
	/** If we detect broken values, we warn the user but only once */
	private boolean warnedUser = false;
	
	/** Returns a prediction for a user */
	@Override
	public float predictRating(int user, int item) {
		
		// if the user or the item was not in the training set, there is no reason to predict anything
		if (!dataModel.getUsers().contains(user) || !dataModel.getItems().contains(item)){
			return Float.NaN;
		}
		
		// compute scalarproduct of qi*pu.
		double[] qiArray = mapOfQiArrays.get(item);
		double[] puArray = mapOfPrecalculatedPuArrays.get(user);
		double scalarPuQi = 0.0;
		for (int a = 0; a < nbFactors; a++) {
			scalarPuQi += qiArray[a] * puArray[a];
		}
		
		return (float) (
				mAvgOfAllPreferences
				+ mapOfBu.get(user)
				+ mapOfBi.get(item)
				+ scalarPuQi);
	}

	/**
	 * Rank the items by rating prediction
	 */
	@Override
	public List<Integer> recommendItems(int user) {
			return super.recommendItemsByRatingPrediction(user);
	}

	@Override
	public void init() throws Exception {
		
		// Initialize the stats
		this.calculateRatingsPerItem();

		double sumOfAllPreferences = 0;
		int numOfAllPreferences = 0;
//		System.out.println("Average");
		for (Integer user : dataModel.getUsers()) {
			for (Rating rating : dataModel.getRatingsOfUser(user)) {
				sumOfAllPreferences += rating.rating;
				numOfAllPreferences++;
			}
		}
		mAvgOfAllPreferences = sumOfAllPreferences / numOfAllPreferences;
		
//		System.out.println("ItemEffects");
		//Deviation (bj) of Item from average. Used for computing buj before parameters have learned.
		Map<Integer, Double> deviationsOfItemsForPrecomputation = itemEffects(regBi);
		
//		System.out.println("UserEffects");
		// Deviation (bu) of User from average. Used for computing buj before parameters have learned.
		Map<Integer, Double> deviationsOfUserForPrecomputation = userEffects(regBu,deviationsOfItemsForPrecomputation);
		
		gradientSolver(iterations, gammaStepSize, lambdaForRegulation, nbFactors, this.rangeOfRandomValues,deviationsOfItemsForPrecomputation,deviationsOfUserForPrecomputation);
//		Debug.log("Factorized NB: Initial training done");
		
		for (int user : dataModel.getUsers()){
			mapOfPrecalculatedPuArrays.put(user, preCalculatePu(user));
		}
		
	}

	/**
	 * Pre-calculate the non-item-dependent side for the prediction function.
	 * @param user
	 * @return
	 */
	private double[] preCalculatePu(int user) {
		double normalizePreferences = Math.pow(dataModel.getRatingsOfUser(user).size(), -0.5); // |R(U)|)|^-0.5 and |N(u)|^-0.5
		
		double[] sumXiArray = new double[nbFactors];
		double[] sumYiArray = new double[nbFactors];
		double[] puArray = new double[nbFactors];

		for (Rating r : dataModel.getRatingsOfUser(user)) {
			double[] xiArray = mapOfXiArrays.get(r.item);
			double[] yiArray = mapOfYiArrays.get(r.item);
			double  buj =
					mAvgOfAllPreferences
					+ mapOfBu.get(user)
					+ mapOfBi.get(r.item);
			for (int a = 0; a < nbFactors; a++) {
				sumXiArray[a] += (r.rating - buj) * xiArray[a];
				sumYiArray[a] += yiArray[a];
			}
		}
		// Part of Prediction-Function independent of item.
		for (int a = 0; a < nbFactors; a++) {
			sumXiArray[a] = normalizePreferences * sumXiArray[a];
			sumYiArray[a] = normalizePreferences * sumYiArray[a];
			puArray[a] = sumXiArray[a] + sumYiArray[a];
		}
		
		return puArray;
	}

	/**
	 * Computes the deviation of preferences for each Item from the average.
	 * Y. Koren: Factor in the Neighbors: Scalable and Accurate Collaborative Filtering (just above chapter 2.2)
	 * 
	 * @param regularizationForBi
	 *            Value for regularization.
	 * @return 
	 */
	public Map<Integer, Double> itemEffects(double regularizationForBi) {
		Map<Integer, Double> deviationsOfItemsForPrecomputation = new HashMap<Integer, Double>();
		for (Integer item : dataModel.getItems()) {
			double sumItemDeviations = 0;
			Set<Rating> ratingsOfItem = this.ratingsOfItems.get(item);
			if (ratingsOfItem != null) {
				for (Rating r : ratingsOfItem) {
					sumItemDeviations += (r.rating - mAvgOfAllPreferences);
				}
				double bi = sumItemDeviations / (regularizationForBi + ratingsOfItem.size());
				deviationsOfItemsForPrecomputation.put(item, bi);
			}
		}
		return deviationsOfItemsForPrecomputation;
	}
	
	/**
	 * This method computes the deviation of the Preferences by each User from
	 * the average.
	 * Y. Koren: Factor in the Neighbors: Scalable and Accurate Collaborative Filtering (just above chapter 2.2)
	 * 
	 * @param regularizationForBu
	 *            value for regularization.
	 * @param deviationsOfItemsForPrecomputation 
	 * @return 
	 */
	public Map<Integer, Double> userEffects(double regularizationForBu, Map<Integer, Double> deviationsOfItemsForPrecomputation) throws Exception {
		Map<Integer, Double> deviationsOfUserForPrecomputation = new HashMap<Integer, Double>();
		for (Integer user : dataModel.getUsers()) {
			double sumUserDeviations = 0;
			Set<Rating> ratingsOfUser = dataModel.getRatingsOfUser(user);
			for (Rating r : ratingsOfUser) {
				double bi = deviationsOfItemsForPrecomputation.get(r.item);
				sumUserDeviations += (r.rating - mAvgOfAllPreferences - bi);
			}
			double bu = sumUserDeviations / (regularizationForBu + ratingsOfUser.size());
			deviationsOfUserForPrecomputation.put(user, bu);
			
		}
		return deviationsOfUserForPrecomputation;
	}

	/**
	 * Learn the parameter on the DataModel
	 * 
	 * @param iteration
	 *            Number of times the steps are repeated.
	 * @param gammaStepSize
	 *            Amount of step size.
	 * @param lambdaForRegulation
	 *            Amount of regulation.
	 * @param deviationsOfUserForPrecomputation 
	 * @param deviationsOfItemsForPrecomputation 
	 * @param dataModel
	 *            The given data model.
	 * @throws TasteException
	 */
	public void gradientSolver(int iteration, double gammaStepSize,
			double lambdaForRegulation, int numOfFactors,
			double rangeOfRandomValues, Map<Integer, Double> deviationsOfItemsForPrecomputation, Map<Integer, Double> deviationsOfUserForPrecomputation) throws Exception {
		
		
		//init items
		for (int item : dataModel.getItems()){
			
			double[] xiArray = new double[numOfFactors];
			for (int a = 0; a < numOfFactors; a++) {
				xiArray[a] = -rangeOfRandomValues + Math.random() * 2 * rangeOfRandomValues;
				
			}
			double[] yiArray = new double[numOfFactors];
			for (int a = 0; a < numOfFactors; a++) {
				yiArray[a] = -rangeOfRandomValues + Math.random() * 2 * rangeOfRandomValues;
			}
			mapOfXiArrays.put(item, xiArray);
			mapOfYiArrays.put(item, yiArray);
			
			double[] qiArray = new double[numOfFactors];
			for (int a = 0; a < numOfFactors; a++) {
				qiArray[a] = (-rangeOfRandomValues + Math
						.random() * 2 * rangeOfRandomValues);
			}
			mapOfQiArrays.put(item, qiArray);
			mapOfBi.put(item,-rangeOfRandomValues + Math.random() * 2 * rangeOfRandomValues );
		}
		
		// init users
		for (int user : dataModel.getUsers()){
			mapOfBu.put(user,-rangeOfRandomValues + Math.random() * 2 * rangeOfRandomValues);
		}
		
		// iterate
		for (int i = 0; i < iteration; i++) {
			
			for (Integer user : dataModel.getUsers()) {
				
				double normalizePreferences = Math.pow(dataModel.getRatingsOfUser(user).size(), -0.5);

				double[] sumXiArray = new double[nbFactors];
				double[] sumYiArray = new double[nbFactors];
				double[] puArray = new double[nbFactors];

				for (Rating r : dataModel.getRatingsOfUser(user)) {
					double[] xiArray = mapOfXiArrays.get(r.item);
					double[] yiArray = mapOfYiArrays.get(r.item);
					double  buj =
							mAvgOfAllPreferences
							+ mapOfBu.get(user)
							+ mapOfBi.get(r.item);
					for (int a = 0; a < nbFactors; a++) {
						sumXiArray[a] += (r.rating - buj) * xiArray[a];
						sumYiArray[a] += yiArray[a];
					}
				}
				// Part of Prediction-Function independent of item.
				for (int a = 0; a < nbFactors; a++) {
					sumXiArray[a] = normalizePreferences * sumXiArray[a];
					sumYiArray[a] = normalizePreferences * sumYiArray[a];
					puArray[a] = sumXiArray[a] + sumYiArray[a];
				}
				
				// sum <- 0
				double sumForGradientStep[] = new double[numOfFactors];
				
				for (Rating r : dataModel.getRatingsOfUser(user)) {
					
					double[] qiArray = mapOfQiArrays.get(r.item);
					double scalarPuQi = 0.0;
					for (int a = 0; a < numOfFactors; a++) {
						// Compute scalarproduct of qi*pu.
						scalarPuQi += (qiArray[a] * puArray[a]);
					}
					
					// Prediction for user u and item i.
					double Pui = mAvgOfAllPreferences + mapOfBu.get(user)
							+ mapOfBi.get(r.item) + scalarPuQi;
					
					if (Double.isNaN(Pui)||Double.isInfinite(Pui)) {
						if(!warnedUser){
							//we will only warn the user once
							warnedUser = true;
							System.err.println("Broken values in factorization of AsymmetricSVD detected (NaN or Infintiy). Your gamma might be set too high.");
						}
					}
						
					// Preference from user u for item i
					double ruj = r.rating;
					// Prediction-Error for user u and item i.
					double Eui = ruj - Pui;
					// Sum for Gradient-Step on xi and yi.
					
					for (int a = 0; a < numOfFactors; a++) {
						sumForGradientStep[a] = sumForGradientStep[a]
								+ (Eui * qiArray[a]);
					}
					// Gradient-Step on qi.
					for (int a = 0; a < numOfFactors; a++) {
						qiArray[a] = qiArray[a]
								+ gammaStepSize * (Eui * puArray[a] - lambdaForRegulation * qiArray[a]);
					}
					// Gradient-Step on bu.
					double bu = mapOfBu.get(user) + gammaStepSize * (Eui - lambdaForRegulation * mapOfBu.get(user));
					
					mapOfBu.put(user, bu);
					
					// Gradient-Step on bi.
					double bi = mapOfBi.get(r.item) + gammaStepSize * (Eui - lambdaForRegulation * mapOfBi.get(r.item));

					mapOfBi.put(r.item, bi);
				}
				
				for (Rating r : dataModel.getRatingsOfUser(user)) {
					double ruj = r.rating;
					double[] xiArray = mapOfXiArrays.get(r.item);
					double[] yiArray = mapOfYiArrays.get(r.item);
					// Baseline estimate for user u and item i.
					double bui = mAvgOfAllPreferences + mapOfBu.get(user) + mapOfBi.get(r.item);
					
					for (int a = 0; a < numOfFactors; a++) {
						// Gradient-Step on xi.
						xiArray[a] = xiArray[a] + gammaStepSize
									* (normalizePreferences * (ruj - bui) * sumForGradientStep[a] - lambdaForRegulation * xiArray[a]);
						// Gradient-Step on yi.
						yiArray[a] = yiArray[a] + gammaStepSize
									* (normalizePreferences * sumForGradientStep[a] - lambdaForRegulation * yiArray[a]);
					}
				}
			}
		}
	}

	/**
	 * Calculate the list of preferences per item
	 * 
	 * @throws Exception
	 */
	protected void calculateRatingsPerItem() throws Exception {
		for (Rating r : dataModel.getRatings()) {
			Set<Rating> ratingsOfItem = this.ratingsOfItems.get(r.item);
			if (ratingsOfItem == null) {
				ratingsOfItem = new HashSet<Rating>();
			}
			this.ratingsOfItems.put(r.item, ratingsOfItem);
			ratingsOfItem.add(r);
		}
	}

	/**
	 * Number of iterations for training
	 * 
	 * @param iterations
	 */
	@R101Setting(displayName="Iterations",
			description="Number of iterations for training", minValue=0,
			type=SettingsType.INTEGER, defaultValue="100")
	public void setIterations(String iterations) {
		this.iterations = Integer.parseInt(iterations);
	}

	/**
	 * Step size for learning
	 * 
	 * @param gammaStepSize
	 */
	@R101Setting(displayName="Gamma",
			description="Step size for learning", minValue=0,
			type=SettingsType.DOUBLE, defaultValue="0.08")
	public void setGamma(String gammaStepSize) {
		this.gammaStepSize = Double.parseDouble(gammaStepSize);
	}

	/**
	 * Regularization value
	 * 
	 * @param lambdaForRegulation
	 */
	@R101Setting(displayName="Lambda",
			description="Regularization value", type=SettingsType.DOUBLE,
			minValue=0, defaultValue="0.002")
	public void setLambda(String lambdaForRegulation) {
		this.lambdaForRegulation = Double.parseDouble(lambdaForRegulation);
	}

	/**
	 * Number of factors for factorization
	 * 
	 * @param n
	 */
	@R101Setting(displayName="Number of factors",
			description="Number of factors for factorization", type=SettingsType.INTEGER,
			defaultValue="100", minValue=0, optional=true)
	public void setNbFactors(String n) {
		this.nbFactors = Integer.parseInt(n);
	}
	
	@Override
	public int getDurationEstimate() {
		return 3;
	}

}
