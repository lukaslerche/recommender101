package org.recommender101.eval.interfaces;

import org.recommender101.gui.annotations.R101HideFromGui;

/**
 * A class that holds the information about an algorithms runtime (meaning how much time was needed for execution)
 * @author Michael Jugovac
 */
@R101HideFromGui
public class RuntimeResult {
		// The runtime for the training in ms
		long trainTime;
		// The runtime for the prediction in ms
		long predictTime;
		// The algorithm of which the runtime was measured 
		String algorithm;
		int evaluationRound;
		
		// =====================================================================================

		public RuntimeResult(String thealgorithm) {
			this.algorithm = thealgorithm;
		}
		
		// =====================================================================================

		public long getTrainTime() {
			return trainTime;
		}
		
		// =====================================================================================

		public void setTrainTime(long trainTime) {
			this.trainTime = trainTime;
		}
		
		// =====================================================================================

		public long getPredictTime() {
			return predictTime;
		}
		
		// =====================================================================================

		public void setPredictTime(long predictTime) {
			this.predictTime = predictTime;
		}
		
		// =====================================================================================

		public long getOverallTime() {
			return predictTime + trainTime;
		}
		
		// =====================================================================================

		public String getAlgorithm() {
			return algorithm;
		}
		
		// =====================================================================================

		public void setAlgorithm(String algorithm) {
			this.algorithm = algorithm;
		}

		// =====================================================================================
		
		public int getEvaluationRound() {
			return evaluationRound;
		}
		
		// =====================================================================================

		public void setEvaluationRound(int evaluationRound) {
			this.evaluationRound = evaluationRound;
		}

		/**
		 * Returns a string representation of the result
		 */
		public String toString() {
			return "\nAlgorithm: " + algorithm + ",\nRound: " + evaluationRound + ",\nTrain Time: "+ trainTime+ ",\nTrain Time: "+ predictTime;
		}
}
