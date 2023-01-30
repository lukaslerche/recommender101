package org.recommender101.recommender.extensions.bprmf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.recommender101.data.Rating;
import org.recommender101.gui.annotations.R101Class;
import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;
import org.recommender101.recommender.AbstractRecommender;
import org.recommender101.tools.Debug;
import org.recommender101.tools.Utilities101;

/**
 * Bayesian Personalized Ranking - Ranking by pairwise classification
 * Literature: Steffen Rendle, Christoph Freudenthaler, Zeno Gantner, Lars
 * Schmidt-Thieme: BPR: Bayesian Personalized Ranking from Implicit Feedback.
 * UAI 2009. http://www.ismll.uni-hildesheim.de/pub/pdfs/Rendle_et_al2009-
 * Bayesian_Personalized_Ranking.pdf
 * 
 * Code based on MyMediaLite http://www.ismll.uni-hildesheim.de/mymedialite/
 * 
 * 
 * @author MW
 * 
 */
@SuppressWarnings("serial")
@R101Class(name="BPRMF Recommender", description="Bayesian Personalized Ranking - Ranking by pairwise classification")
public class BPRMFRecommender extends AbstractRecommender implements Serializable {

	// / Sample uniformly from users, for the strategy of the original paper set
	// false
	public boolean UniformUserSampling = true;

	private final static Random random = new Random();

	// Regularization parameter for the bias term
	public double biasReg = 0;

	// number of columns in the latent matrices
	public int numFeatures = 100;

	// number of iterations over the data
	public int initialSteps = 100;

	// number of users
	protected int numUsers;

	// number of items
	public int numItems;

	// datamanagement-object
	public DataManagement data;

	// Learning rate alpha
	public double learnRate = 0.05;

	// Regularization parameter for user factors
	public double regU = 0.0025;

	// Regularization parameter for positive item factors
	public double regI = 0.0025;

	// Regularization parameter for negative item factors</summary>
	public double regJ = 0.00025;

	// If set (default), update factors for negative sampled items during
	// learning
	public boolean updateJ = true;
	
	// The two factors to modify the sampling of i and j.
	// default = uniform sampling = 0
	// if both this and popI/J are set, this will take over
	public double gaussDenominatorI = 0;
	public double gaussDenominatorJ = 0;
	
	// The two switches to enable pop sampling for i and j.
	// default = uniform sampling = false;
	// if both this and gaussDenominatorI/J are set, the gaussian sampling will take over
	public boolean popI = false;
	public boolean popJ = false;

	//default is true for legacy reasons
	public boolean hideKnownItems = true;
	
	@Override
	/**
	 * Not implemented
	 * Returns rating of item by user
	 * @param user Number  - the user ID
	 * @param item Number  - the item ID
	 * @returns Float rating of the given item by the given user
	 */
	public float predictRating(int user, int item) {
		return Float.NaN;
	}

	/**
	 * Returns rating of item by user -> not usable for rating prediction
	 * @param user Number  - the user ID
	 * @param item Number  - the item ID
	 * @returns Float rating of the given item by the given user
	 */
	public float predictRatingBPR(int user, int item) {
		// Note: Predictions are only helpful for ranking and not for prediction
		// convert IDs in mapped values
		int itemidx = data.itemIndices.get(item);
		Integer useridx = data.userIndices.get(user);
		
		if (useridx != null) {
			return (float) (data.item_bias[itemidx] + data.rowScalarProduct( useridx, itemidx));
		}
		else {
			// This might happen during training test splits for super-sparse (test) data
//			System.out.println("-- No entry for user: " + user);
			return Float.NaN;
		}
	}
	// =====================================================================================

	/**
	 * This is similar to AbstractRecommender.recommendItemsByRatingPrediction but uses the internal function
	 */
	public List<Integer> recommendByPrediction(int user) {
		List<Integer> result = new ArrayList<Integer>();

		// If there are no ratings for the user in the training set,
		// there is no point of making a recommendation.
		Set<Rating> ratings = getDataModel().getRatingsOfUser(user);
		// If we have no ratings...
		if (ratings == null || ratings.size() == 0) {
			return Collections.emptyList();
		}
//		System.out.println("I have " + ratings.size() + " total ratings of user " + user);
		
		// Calculate rating predictions for all items we know
		Map<Integer, Float> predictions = new HashMap<Integer, Float>();
		float pred = Float.NaN;
		
		// Go through all the items
		for (Integer item : dataModel.getItems()) {
			boolean userHasAlreadyRatedItem = false;
			
			//if we include items that the user has already rated, no checking for these items is necessary
			if(hideKnownItems){
				// We will not recommend items repeatedly here
				float rating = dataModel.getRating(user, item);
				if (rating != -1) {
					userHasAlreadyRatedItem = true;
				}
			}
			
			if (!userHasAlreadyRatedItem) {
				// make a prediction and remember it in case the recommender
				// could make one
				pred = predictRatingBPR(user, item);
				if (!Float.isNaN(pred)) {
					predictions.put(item, pred);
				}
			}
		}
		
		predictions = filterElementsByRelevanceThreshold(predictions, user);
		predictions = Utilities101.sortByValueDescending(predictions);
		
		for (Integer item : predictions.keySet()) {
			result.add(item);
		}
		
		return result;
	}
	
	
	/**
	 * This method recommends items.
	 */
	@Override
	public List<Integer> recommendItems(int user) {
		return recommendByPrediction(user);
	}

	// =====================================================================================

	@Override
	/**
	 * Initialization of the needed objects and variables
	 * 
	 */
	public void init() {
		
		data = new DataManagement(random);

		// ascertain number of users and items
		numItems = dataModel.getItems().size();
		numUsers = dataModel.getUsers().size();

//		System.out.println("Users, items: " + numUsers + " " + numItems + " ratings " + dataModel.getRatings().size());
		
		// if one of the gaussian sampling parameters is set, tell the
		// DataManagement class to prepare the necessary additional data structures
		if (gaussDenominatorI != 0 || gaussDenominatorJ != 0 || popI || popJ){
			data.useAdvancedSampling = true;
		}

		// Initialization of datamanagement-object
		data.init(dataModel, numUsers, numItems, numFeatures);

//		System.out.println("Init done BPR");
		// trainig of the data
		train();

		Debug.log("BPRMF:init: Initial training done");

	}

	// =====================================================================================


	// =====================================================================================
	/**
	 * Training of the given data
	 * 
	 */
	public void train() {
		for (int i = 0; i < initialSteps; i++) {
//			System.out.println("Iterating BPR: " + i);
			iterate();
		}
	}

	// =====================================================================================

	/**
	 * Perform one iteration of stochastic gradient ascent over the training
	 * data
	 * 
	 */
	public void iterate() {
		// number of all positive ratings
		int num_pos_events = data.numPosentries;

		int user_id, pos_item_id, neg_item_id;

		
		if (UniformUserSampling) {

			//performing convergence-heuristic of LearnBPR
			for (int i = 0; i < num_pos_events; i++) {

				// sampling a triple, consisting of a user, a viewed item and an
				// unseen one, by given user
				int[] triple = sampleTriple();
				user_id = triple[0];
				pos_item_id = triple[1];
				neg_item_id = triple[2];

				updateFactors(user_id, pos_item_id, neg_item_id, true, true, updateJ);
			}

		} else {
			// runs over all possible user-item-combinations
			for (int k = 0; k < data.boolMatrix_numUsers; k++) { // was before: data.boolMatrix.length

				for (int l = 0; l < data.boolMatrix_numItems; l++) { // was before: data.boolMatrix[k].length

					user_id = k;
					pos_item_id = l;
					neg_item_id = -1;

					// if the user has not seen the item, another item gets
					// chosen
					if (!data.boolMatrix.getBool(user_id,pos_item_id)) // was before: !data.boolMatrix[user_id][pos_item_id]
						continue;

					// sampling a triple for a given user and seen item
					int[] sampleTriple = sampleOtheritem(user_id, pos_item_id,
							neg_item_id);
					user_id = sampleTriple[0];
					pos_item_id = sampleTriple[1];
					neg_item_id = sampleTriple[2];

					updateFactors(user_id, pos_item_id, neg_item_id, true, true, updateJ);
				}
			}
		}

	}

	// =====================================================================================

	/**
	 * latent matrices and item_bias are updated according to the stochastic
	 * gradient descent update rule
	 * 
	 * @param u
	 *            Number - the mapped userID
	 * @param i
	 *            Number - the mapped itemID of a viewed item
	 * @param j
	 *            Number - the mapped itemID of an unviewed item
	 * @param update_u
	 *            Boolean - should u be updated
	 * @param update_i
	 *            Boolean - should i be updated
	 * @param update_j
	 *            Boolean - should j be updated
	 */
	public void updateFactors(int u, int i, int j, boolean update_u,
			boolean update_i, boolean update_j) {

		// calculating the estimator
		double x_uij = data.item_bias[i] - data.item_bias[j]
				+ data.rowScalarProductWithRowDifference(u, i, j);

		double one_over_one_plus_ex = 1 / (1 + Math.exp(x_uij));

		// adjust bias terms for seen item
		if (update_i) {
			double update = one_over_one_plus_ex - biasReg * data.item_bias[i];
			data.item_bias[i] += (learnRate * update);
		}

		// adjust bias terms for unseen item
		if (update_j) {
			double update = -one_over_one_plus_ex - biasReg * data.item_bias[j];
			data.item_bias[j] += (learnRate * update);
		}

		// adjust factors
		for (int f = 0; f < numFeatures; f++) {
			double w_uf = data.latentUserVector[u][f];
			double h_if = data.latentItemVector[i][f];
			double h_jf = data.latentItemVector[j][f];

			//adjust component of user-vector
			if (update_u) {
				double update = (h_if - h_jf) * one_over_one_plus_ex - regU
						* w_uf;
				data.latentUserVector[u][f] = (w_uf + learnRate * update);
			}

			//adjust component of seen item-vector
			if (update_i) {
				double update = w_uf * one_over_one_plus_ex - regI * h_if;
				data.latentItemVector[i][f] = (float) (h_if + learnRate
						* update);
			}
			//adjust component of unseen item-vector	
			if (update_j) {
				double update = -w_uf * one_over_one_plus_ex - regJ * h_jf;
				data.latentItemVector[j][f] = (float) (h_jf + learnRate
						* update);
			}
		}
	}

	// =====================================================================================

	/**
	 * finds another unseen item
	 * 
	 * @param u
	 *            Number - the mapped userID
	 * @param i
	 *            Number - the mapped itemID of a viewed item
	 * @param j
	 *            Number - the mapped itemID of an unviewed item
	 * @return sampleTriple Array - an array containing the mapped userID, the
	 *         mapped view itemId and the mapped unviewed itemID
	 */
	public int[] sampleOtheritem(int u, int i, int j) {
		int[] sampleTriple = new int[3];
		sampleTriple[0] = u;
		sampleTriple[1] = i;
		sampleTriple[2] = j;
		boolean item_is_positive = data.boolMatrix.getBool(u,i); // was before: data.boolMatrix[u][i]

		do
			sampleTriple[2] = random.nextInt(numItems);
		while (data.boolMatrix.getBool(u,sampleTriple[2]) == item_is_positive); // was before: data.boolMatrix[u][sampleTriple[2]]

		return sampleTriple;
	}

	// =====================================================================================

	/**
	 * finds an user who has viewed at least one item but not all
	 * 
	 * @return u Number - the mapped userID
	 */
	public int sampleUser() {
		while (true) {

			int u = random.nextInt(numUsers);
			if (!data.userMatrix.containsKey(u))
				continue;
			List<Integer> viewedItemsList = data.userMatrix.get(u);

			if (viewedItemsList == null || viewedItemsList.size() == 0
					|| viewedItemsList.size() == numItems)
				continue;
			return u;
		}
	}

	// =====================================================================================

	/**
	 * calls the methods which find an user, a viewed item and an unviewed item
	 * 
	 * @return sampleTriple Array - an array containing the mapped userID, the
	 *         mapped view itemId and the mapped unviewed itemID
	 */
	public int[] sampleTriple() {
		int[] triple = new int[3];
		triple[0] = sampleUser();
		return sampleItempair(triple);
	}

	// =====================================================================================

	/**
	 * finds a seen item and an unseen item
	 * 
	 * @return sampleTriple Array - an array containing the mapped userID, the
	 *         mapped view itemId and the mapped unviewed itemID
	 */
	public int[] sampleItempair(int[] triple) {
		int u = triple[0];

		List<Integer> user_items = data.userMatrix.get(u);

		// use the gaussian distribution to aquire the i item of the (u,i,j)
		// triple from the less popular ones (aka the unpopular good alternative to the popular item j)
		if (gaussDenominatorI > 0) {
			triple[1] = gaussRandItem(data.userPopularityMatrixAscending.get(u), gaussDenominatorI);
			}
		else if(popI){
			triple[1] = aggregationRandItem(data.aggregatedUserPopularityMatrixAscending.get(u), data.aggregatedUserPopularitySum.get(u));
		}
		// else default uniformly random drawing
		else {
			triple[1] = user_items.get((random.nextInt(user_items.size())));
		}
		do {
			// use the gaussian distribution to aquire the j item of the (u,i,j)
			// triple from the more popular ones (aka the popular, but unliked item)
			if (gaussDenominatorJ > 0){
				triple[2] = gaussRandItem(data.popularityListDescending,gaussDenominatorJ);
			}
			else if(popJ){
				triple[2] = aggregationRandItem(data.aggregatedPopularityMapDescending, data.numPosentries);
			}
			// else default uniformly random drawing
			else{
				triple[2] = random.nextInt(numItems);
			}
		} while (user_items.contains(triple[2]));

		return triple;
	}
	
	/**
	 * returns an item from the list with a gauss distribution starting at the
	 * most unpopular item with std. dev. of items.size / denominator that means
	 * that there is a circa 70% chance that items from 0 to items.size / denominator
	 * are choosen
	 * 
	 * @param list
	 *            is the list of items to draw from
	 * @param denominator
	 *            is the denominator of the variance. Bigger: Restrict more to
	 *            top of the list, Smaller: draw more uniformly.
	 * @return an item drawn with a gaussian distribution from the list. If the
	 *         distribution chose an index > list.size then the last item of the
	 *         list will be drawn.
	 */
	private int gaussRandItem(List<Integer> list, double denominator) {
		// try 10 times to draw gaussian from the list
		for (int i = 0; i < 10; i++){
			double gRand = random.nextGaussian();
			double gRandExpanded = gRand * list.size() / denominator;
			int listIndex = Math.abs((int) Math.round(gRandExpanded));

			if (listIndex < list.size()){
				return list.get(listIndex);
			}
		}
		// if that fails because the index was always out of list bounds, then just draw random
		return list.get(random.nextInt(list.size()));
	}
	
	// draw by popularity
	/**
	 * Draw radomly from the keys of a provided (linked) map with a probability
	 * based on the aggregated sum in the values of the map. The second
	 * parameter is the sum over all (non-aggregated) values, which is the last
	 * value in the map.
	 * 
	 * @param map
	 *            a linked map with arbitrary keys and an integer value that is
	 *            aggregated.
	 * @param aggregatedSize
	 *            the sim over all (non-aggregated) values of the map (to save
	 *            runtime).
	 * @return the key of the drawn list element or -1 in case of error.
	 */
	private int aggregationRandItem(Map<Integer, Integer> map,
			int aggregatedSize) {
		int pRandom = random.nextInt(aggregatedSize) + 1;
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			if (entry.getValue() >= pRandom) {
				return entry.getKey();
			}
		}
		System.err.println("popularityRandItem() did not yield a correct value!");
		return -1;
	}
	
	// =====================================================================================

	/**
	 * Setter for factory
	 * 
	 * @param n
	 */
	@R101Setting(defaultValue="100", type=SettingsType.INTEGER,
			description="Sets the number of features", displayName="Number of features",
			minValue=0)
	public void setNumFeatures(String n) {
		this.numFeatures = Integer.parseInt(n);
	}

	/**
	 * Setter for regI
	 * 
	 * @param n
	 */
	@R101Setting(displayName="RegI", type=SettingsType.DOUBLE,
			defaultValue="0.0025", minValue=0)
	public void setRegI(String n) {
		this.regI = Double.parseDouble(n);
	}
	
	/**
	 * Setter for regJ
	 * 
	 * @param n
	 */
	@R101Setting(displayName="RegJ", type=SettingsType.DOUBLE,
			defaultValue="0.00025", minValue=0)
	public void setRegJ(String n) {
		this.regJ = Double.parseDouble(n);
	}
	
	/**
	 * Setter for regU
	 * 
	 * @param n
	 */
	@R101Setting(displayName="RegU", type=SettingsType.DOUBLE,
			defaultValue="0.0025", minValue=0)
	public void setRegU(String n) {
		this.regU = Double.parseDouble(n);
	}
	
	/**
	 * Setter for UpdateJ
	 * 
	 * @param n
	 */
	@R101Setting(displayName="UpdateJ", defaultValue="true",
			type=SettingsType.BOOLEAN)
	public void setUpdateJ(String n) {
		this.updateJ = Boolean.parseBoolean(n);
	}
	
	
	/**
	 * Setter for BiasReg
	 * 
	 * @param n
	 */
	@R101Setting(type=SettingsType.DOUBLE, displayName="Bias Reg",
			defaultValue="0")
	public void setBiasReg(String n) {
		this.biasReg = Double.parseDouble(n);
	}
	
	/**
	 * Setter for LearnRate
	 * 
	 * @param n
	 */
	@R101Setting(displayName="Learn rate", defaultValue="0.05", type=SettingsType.DOUBLE,
			minValue=0)
	public void setLearnRate(String n) {
		this.learnRate = Double.parseDouble(n);
	}
	

	/**
	 * Setter for the initial steps
	 * 
	 * @param n
	 */
	@R101Setting(type=SettingsType.INTEGER, displayName="Initial steps",
			defaultValue="100", minValue=0)
	public void setInitialSteps(String n) {
		this.initialSteps = Integer.parseInt(n);
	}
	
	/**
	 * Setter for the uniform Sampling
	 * 
	 * @param n
	 */
	@R101Setting(type=SettingsType.BOOLEAN, displayName="Uniform sampling",
			description="Enables uniform sampling", defaultValue="false")
	public void setUniformSampling(String n) {
		this.UniformUserSampling = Boolean.parseBoolean(n);
	}
	
	@Override
	public int getDurationEstimate() {
		return 3;
	}
	
	/**
	 * Should the global relevance threshold be chosen or not
	 * @param u
	 */
	@R101Setting(displayName="Use relevance threshold", type=SettingsType.BOOLEAN,
			defaultValue="false")
	public void setUseRelevanceThreshold(String u)  throws Exception {
		data.useRatingThreshold = Boolean.parseBoolean(u);
	}
	
	/**
	 * Set the denominator for the variance of the gauss sampling of i
	 * @param d
	 */
	@R101Setting(displayName="Gauss Denominator for i", type=SettingsType.DOUBLE,
			defaultValue="0",minValue=0)
	public void setGaussDenominatorI(String i)  throws Exception {
		this.gaussDenominatorI = Double.parseDouble(i);
	}
	
	/**
	 * Set the denominator for the variance of the gauss sampling of j
	 * @param d
	 */
	@R101Setting(displayName="Gauss Denominator for j", type=SettingsType.DOUBLE,
			defaultValue="0",minValue=0)
	public void setGaussDenominatorJ(String i)  throws Exception {
		this.gaussDenominatorJ = Double.parseDouble(i);
	}
	
	/**
	 * Activate the usage of popularity sampling of i
	 * @param d
	 */
	@R101Setting(displayName="Use popularity sampling for i", type=SettingsType.BOOLEAN,
			defaultValue="false")
	public void setPopI(String b)  throws Exception {
		this.popI = Boolean.parseBoolean(b);
	}
	
	/**
	 * Activate the usage of popularity sampling of j
	 * @param d
	 */
	@R101Setting(displayName="Use popularity sampling for j", type=SettingsType.BOOLEAN,
			defaultValue="false")
	public void setPopJ(String b)  throws Exception {
		this.popJ = Boolean.parseBoolean(b);
	}

	@Override
	protected void hideKnownItems(boolean value) {
		hideKnownItems = value;
	}
}