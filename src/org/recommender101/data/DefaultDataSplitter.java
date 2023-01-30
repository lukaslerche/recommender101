/** DJ **/
package org.recommender101.data;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.recommender101.gui.annotations.R101Class;

/**
 * An abstract class for data splitting, e.g. for cross validation
 * @author DJ
 *
 */
@R101Class(name="Default Data Splitter")
public class DefaultDataSplitter extends DataSplitter {
	
	public static final Random random = new Random();
	
	/**
	 * A handle to the condensed splits
	 */
	public List<Set<Rating>> smallSplits = null;

	// =====================================================================================

	/**
	 * A method that splits the data randomly in n folds
	 * @param n the number of folds
	 * @param dataModel the data model containing the original data.
	 * @return the list
	 */
	public List<Set<Rating>> splitData (DataModel dataModel) throws Exception {
		
		List<Set<Rating>> result = new ArrayList<Set<Rating>> ();

		// Default behavior
		result = createNFolds(nbFolds, dataModel);
		return result;
		
	} 
	
	/**
	 * A method that splits the data in n folds
	 * @param theratings a list of ratings
	 * @param nbFolds
	 * @return
	 */
	protected List<Set<Rating>> createNFolds(int nbFolds, DataModel dataModel) {
		List<Set<Rating>> result = new ArrayList<Set<Rating>>();
		// Create the empty lists
		for (int i=0;i<nbFolds;i++) {
			result.add(new ObjectOpenHashSet<Rating>(dataModel.getRatings().size()/nbFolds));
		}
		
		// Split data randomly across users
		if (this.globalRandomSplit) {
			// Use an array to shuffle things
			List<Rating> ratingsCopy = new ArrayList<Rating>(dataModel.getRatings());
			// Shuffle the ratings first
			Collections.shuffle(ratingsCopy);
			// distribute the ratings round robin to the bins
			int i = 0;
			for (Rating r : ratingsCopy)  {
				result.get(i%nbFolds).add(r);
				i++;
			}
			return result;
		}
		// Distribute things per user
		else {
			// Get the ratings each user
			for (Integer user : dataModel.getUsers()){
				// Get a copy to shuffle
				List<Rating> ratingsCopy = new ArrayList<Rating> (dataModel.getRatingsOfUser(user));
				Collections.shuffle(ratingsCopy);
				// distribute to the bins
				// do not start with 0 all the time as this leads to unbalanced bins
				int i = random.nextInt(nbFolds);
				for (Rating r : ratingsCopy) {
					result.get(i%nbFolds).add(r);
					i++;
				}
			}
			return result;
		}

	}
	
	public List<Set<Rating>>getSpecialTestSplits() {
		return this.smallSplits;
	}

}
