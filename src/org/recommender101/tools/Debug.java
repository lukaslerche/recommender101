/** DJ **/
package org.recommender101.tools;

import org.recommender101.eval.impl.Recommender101Impl;

/**
 * A debug helper
 * @author DJ
 *
 */
public class Debug {
	
  // =====================================================================================

	static public boolean DEBUGGING_ON = false;
	//static public boolean TIME_ON = true;
	static private long startTime = 0;
	
	static {
		if (Recommender101Impl.properties != null) {
			String debugOnStr = Recommender101Impl.properties.getProperty("Debug.Messages");
			if ("on".equalsIgnoreCase(debugOnStr)) {
				DEBUGGING_ON = true;
			}
			else if  ("off".equalsIgnoreCase(debugOnStr)) {
				DEBUGGING_ON = false;
			}
		}
	}
	
	public static void initTime(){
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Simply prints stuff
	 */
	public static void log(String msg) {
		if (DEBUGGING_ON) {
			String time = "";
			if (startTime != 0){
				long secs = (System.currentTimeMillis() - startTime)/1000;
				time = secs+" - ";
			}
			
			System.out.println("["+time+"DEBUG] "  + msg);
			
		}
	}
	
	/**
	 * prints error stuff
	 */
	public static void error(String msg) {
		String time = "";
		if (startTime != 0){
			long secs = (System.currentTimeMillis() - startTime)/1000;
			time = secs+" - ";
		}
		
		if (DEBUGGING_ON) {
			System.err.println("["+time+"ERROR] "  + msg);
		}
	}
}
