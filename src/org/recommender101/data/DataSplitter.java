/** DJ **/
package org.recommender101.data;

import java.util.List;
import java.util.Set;

import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;

/**
 * Interface for data splitters
 * @author DJ
 *
 */
public abstract class DataSplitter {

	// A method that splits the dataset for cross validation into n bins
	public abstract List<Set<Rating>> splitData (DataModel dataModel) throws Exception;
	// Use a global split and not a per-user split; could be set to false in later experiments as default
	protected boolean globalRandomSplit = false;
	
	/**
	 * Remember the number of folds
	 */
	int nbFolds = 5;
	
	/**
	 * Get the folds
	 * @return
	 */
	public int getNbFolds() {
		return nbFolds;
	}

	/**
	 * Alternative setter for the number of folds
	 * @param nbFolds
	 */
	@R101Setting(type=SettingsType.INTEGER, defaultValue="5", minValue=0,
			displayName="Number of folds", description="Sets the number of folds")
	public void setNbFolds(String nbFolds) {
		this.nbFolds = Integer.parseInt(nbFolds);
	}

	// Get small splits if any
	public List<Set<Rating>> getSpecialTestSplits() {
		return null;
	};

	// Split data randomly across users or not
	@R101Setting(type=SettingsType.BOOLEAN, defaultValue="false",
			displayName="Global Random Split", description="Split data randomly across users or not")
	public void setGlobalRandomSplit(String b) {
		this.globalRandomSplit = Boolean.parseBoolean(b);
	}
}
