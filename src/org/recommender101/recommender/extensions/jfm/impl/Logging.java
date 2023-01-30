package org.recommender101.recommender.extensions.jfm.impl;

import org.recommender101.tools.Debug;

/**
 * Just a class to delegate logging. Makes things easier, because libfm doesn't only print strings but also floats and such.
 * It is also helpful in differentiating between log outputs as a prefix is appended.
 * @author Michael Jugovac
 */
public class Logging {

	private static String _prefix = "[LibFM Output] ";
	
	/**
	 * Simply a delegate to log output
	 * @param msg The message to be logged
	 */
	public static void log(String msg){
		Debug.log(_prefix + msg);
	}

	/**
	 * Simply a delegate to log output of floats, which is not supported by R101 by default
	 * @param msg The float to be logged
	 */
	public static void log(Float f) {
		Debug.log(_prefix + f);
	}
}
