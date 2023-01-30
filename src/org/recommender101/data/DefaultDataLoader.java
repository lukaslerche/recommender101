/** DJ **/
package org.recommender101.data;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Set;

import org.recommender101.gui.annotations.R101Class;
import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;
import org.recommender101.tools.Debug;
import org.recommender101.tools.Utilities101;

/**
 * A default data loader capable of loading movielens files
 * Format: user<tab>item<tab>rating<tab>timestamp
 * @author DJ
 *
 */
@R101Class (name="Default Data Loader", description="A default data loader capable of loading movielens files.")
public class DefaultDataLoader  {

	// A default location
	protected String filename = "data/movielens/ratings.txt";
	protected int minNumberOfRatingsPerUser = -1;
	protected int minNumberOfRatingsPerItem = -1;
	protected int sampleNUsers = -1;
	protected double density = 1.0;
	
	public static int maxLines = -1;

	// Should we transform the data 
	// 0 no
	// > 0: This is the threshold above which items are relevant 
	public int binarizeLevel = 0;
	
	// Should we remove 0 -valued ratings?
	public boolean useUnaryRatings = false;
	protected String separatorString = "\t";
	
	
	/**
	 * An empty constructor
	 */
	public DefaultDataLoader() {
	}
	
	
	public void applyConstraints(DataModel dm) throws Exception{
		// Apply sampling procedure
		if (this.sampleNUsers > -1) {
			dm = Utilities101.sampleNUsers(dm, sampleNUsers);
			Debug.log("DataLoader: Retaining " + dm.getUsers().size()
					+ " sampled users and " + dm.getRatings().size() + " ratings");
			dm.recalculateUserAverages();
		}
		
		//used to issue a warning to user that reduction may be done multiple times
		boolean alreadyTriedToReduce = false;
		
		//we need to apply these contraints in a loop because one might influence the other
		do{
			if(alreadyTriedToReduce){
				Debug.log("DataLoader: Applying rating constraints again.");
			}
			alreadyTriedToReduce = true;
			// Apply min number of ratings constraint
			if (minNumberOfRatingsPerUser > 0) {
				Debug.log("DataLoader: Applying minimum rating constraint for users ("+minNumberOfRatingsPerUser+")");
				int removed = Utilities101.applyMinRatingsPerUserConstraint(dm, minNumberOfRatingsPerUser);
				Debug.log("DataLoader: Removed "+  removed + " users. " + dm.getRatings().size()
						+ " ratings of " + dm.getUsers().size() + " users remain.");
				dm.removeDeadUsers();
				dm.recalculateUserAverages();
			}
			
			// Apply min number of ratings constraint
			if (minNumberOfRatingsPerItem > 0) {
				Debug.log("DataLoader: Applying minimum rating constraint for items ("+minNumberOfRatingsPerItem+")");
				int removed = Utilities101.applyMinRatingsPerItemConstraint(dm, minNumberOfRatingsPerItem);
				Debug.log("DataLoader: Removed "+  removed + " items. " + dm.getRatings().size()
						+ " ratings of " + dm.getItems().size() + " items remain.");
				dm.removeDeadUsers();
				dm.recalculateUserAverages();
			}
		}while(dm.getMinUserRatings() < minNumberOfRatingsPerUser);

		// Apply sampling of data to vary density
		if (this.density < 1.0) {
			dm = Utilities101.applyDensityConstraint(dm, this.density);
			dm.removeDeadUsers();
			dm.recalculateUserAverages();
		}
		
		if (this.binarizeLevel > 0) {
			Debug.log("Binarizing at level: " + this.binarizeLevel);
			this.binarize(dm);
		}
	}
		
	// =====================================================================================

	/**
	 * The method loads the MovieLens data from the specified file location.
	 * The method can be overwritten in a subclass
	 */
	public void loadData(DataModel dm) throws Exception {
		int counter = 0;
		// Read the file line by line and add the ratings to the data model.
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		line = reader.readLine();
		String[] tokens;
		while (line != null) {
			// Skip comment lines
			if (line.trim().startsWith("//")) {
				line = reader.readLine();
				continue;
			}
			tokens = line.split(separatorString);
			// First, add the ratings.
			dm.addRating(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Float.parseFloat(tokens[2]));
			line = reader.readLine();
			counter++;
//			// debugging here..
			if (maxLines != -1) {
				if (counter >= maxLines) {
					System.out.println("DataLoader: Stopping after " + (counter)  + " lines for debug");
					break;
				}
			}
		}
		Debug.log("DefaultDataLoader:loadData: Loaded " + counter + " ratings");
		Debug.log("DefaultDataLoader:loadData: " + dm.getUsers().size() + " users and " + dm.getItems().size() + " items.");
		reader.close();
		applyConstraints(dm);
	}
	
	// =====================================================================================

	
	/**
	 * Sets the file name
	 * @param name
	 */
	@R101Setting(displayName="Filename", description="Sets the filename of the dataset", 
			type=SettingsType.FILE)
	public void setFilename(String name) {
		filename = name;
	}

	// =====================================================================================

	/**
	 *  Returns the filename 
	 * 
	 */
	public String getFilename() {
		return filename;
	}
	// =====================================================================================

	/**
	 * To be used by the class instantiator. Defines the minimum number of ratings a user 
	 * must have to remain in the dataset
	 * @param n the min number of ratings per user
	 */
	@R101Setting(displayName="Minimum number of ratings per user",
			description="Defines the minimum number of ratings a user must have to remain in the dataset",
			defaultValue="-1", type=SettingsType.INTEGER, minValue=-1, maxValue=Integer.MAX_VALUE)
	public void setMinNumberOfRatingsPerUser(String n) {
		minNumberOfRatingsPerUser = Integer.parseInt(n);
	}
	
	/**
	 * To be used by the class instantiator. Defines the minimum number of ratings an item 
	 * must have to remain in the dataset
	 * @param n the min number of ratings per item
	 */
	@R101Setting(displayName="Minimum number of ratings per item",
			description="Defines the minimum number of ratings an item must have to remain in the dataset",
			defaultValue="-1", type=SettingsType.INTEGER, minValue=-1, maxValue=Integer.MAX_VALUE)
	public void setMinNumberOfRatingsPerItem(String n) {
		minNumberOfRatingsPerItem = Integer.parseInt(n);
	}
	
	
	/**
	 * Instructs the load to sample a given number of users
	 * @param n how many users to keep 
	 */
	@R101Setting(defaultValue="-1", displayName="Sample N users", 
			description="Instructs the loader to sample a given number of users", type=SettingsType.INTEGER,
			minValue=-1, maxValue=Integer.MAX_VALUE)
	public void setSampleNUsers(String n) {
		this.sampleNUsers = Integer.parseInt(n);
	}
	
	/**
	 * Set the density
	 * @param d
	 */
	@R101Setting(displayName="Density", description="Sets the density", type=SettingsType.DOUBLE,
			minValue=0.0, maxValue=1.0, defaultValue="1.0")
	public void setDensity(String d) {
		this.density = Double.parseDouble(d);
	}
	
	/**
	 * Set the binarization method
	 * @param b
	 */
	@R101Setting(defaultValue="0", type=SettingsType.INTEGER, description="Sets the binarization method",
			displayName="Binarize Level", minValue=0, maxValue=Integer.MAX_VALUE)
	public void setBinarizeLevel(String b) {
		this.binarizeLevel = Integer.parseInt(b);
	}
	
	/**
	 * Binarizes the data model after loading
	 * @throws Exception
	 */
	public void binarize(DataModel dm) throws Exception {
		
		Set<Rating> ratingsCopy = new ObjectOpenHashSet<Rating>(dm.getRatings());
		
		// Go through the ratings
		for (Rating r : ratingsCopy) {
			// Option one - every rating is relevant
			
			if (r.rating >= this.binarizeLevel) {
				r.rating = 1;
			}
			else {
				// Remove rating in case we only have positive feedback
				if (this.useUnaryRatings) {
					dm.removeRating(r);
				}
				// Otherwise, set it to 0
				else {
					r.rating = 0;
				}
			}
		}
		// Recalculate things
		dm.recalculateUserAverages();
	}
	
	/**
	 * Sould we use unary ratings? (If yes, we delete all 0 ratings)
	 * @param b
	 */
	@R101Setting(description="All 0 ratings will be deleted", type=SettingsType.BOOLEAN,
			defaultValue="false", displayName="Unary Ratings")
	public void setUnaryRatings(String b) {
		this.useUnaryRatings = Boolean.parseBoolean(b);
	}
	
	/**
	 * The string that separates columns in the input file. E.g. user::item::rating -> "47::11::4" => Separator String = "::"
	 * @param value
	 */
	@R101Setting(description="The string that separates columns in the input file", type=SettingsType.TEXT,
			defaultValue="\t", displayName="Separator String")
	public void setSeparatorString(String value) {
		this.separatorString = value;
	}
	
	


}
