/** DJ **/
package org.recommender101.data.extensions.dataloader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.recommender101.data.DataModel;
import org.recommender101.data.DefaultDataLoader;
import org.recommender101.data.Rating;
import org.recommender101.gui.annotations.R101Class;
import org.recommender101.tools.Debug;

/**
 * A data loader extension which loads the Ivi sqlite database file
 * @author mludewig
 *
 */
@R101Class(name="Data Loader for ivi sqlite file with timestamp", description="Data loader extension which loads the data from the ivi sqlite database.")

public class IviDataLoader extends DefaultDataLoader {
	
	private static final String SELECT_RATINGS = "	select 			"
											   + "		r.user_id, 	"
										       + "		r.item_id,	"
										       + "		r.value,	"
										       + "		r.added 	"
										       + "	from 			"
										       + "		rate r 		"
										       + "	order by 		"
										       + "		r.user_id,	"
										       + "		r.item_id	";
	
	public static final String DM_EXTRA_INFO_TIMESTAMP_KEY = "RatingTimeStamps";
	
	// =====================================================================================

	/**
	 * The method loads the Ivi data from the specified file location and also reads the time information
	 */
	@Override
	public void loadData(DataModel dm) throws Exception {
		
		Connection con = null;
		Statement stmt = null;
		
		try {

			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:" + getFilename());

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(SELECT_RATINGS);
			
			// We construct a hashmap that stores a mapping of
			// "user:item"->"timestamp". The key is the concatenated string
			// of user and item id.
			Map<Rating, Long> timestamps = new HashMap<Rating, Long>();
			dm.addExtraInformation(DM_EXTRA_INFO_TIMESTAMP_KEY, timestamps);
			
			Rating r = null;
			int c = 0;
			
			while (rs.next()) {
								
				r = dm.addRating( rs.getInt(1) , rs.getInt(2), rs.getInt(3) );
				timestamps.put(r, rs.getLong(4));
				c++;
				
			}
			
			Debug.log("IviDataLoader:loadData: Loaded " + c + " ratings");
			
			rs.close();
			stmt.close();
			con.close();
			
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		
	}

}
