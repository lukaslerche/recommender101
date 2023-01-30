package org.recommender101.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.recommender101.eval.impl.Recommender101Impl;
import org.recommender101.eval.interfaces.EvaluationResult;
import org.recommender101.eval.interfaces.RuntimeResult;

/**
 * Helper class for csv output
 * 
 * @author Christian Drescher
 * 
 */
public class CSVOutputWriter {

	/**
	 * Writes and appends results to csv file
	 * 
	 * @param experimentTitle Title as headline for csv output
	 * @param lastResults Evaluation results
	 * @param csvPath Path to CSV file
	 * @param csvMode 
	 * @throws IOException
	 */
	public static void writeToCSV(String experimentTitle,
			List<EvaluationResult> lastResults, String csvPath, boolean append, List<String> evalMethods)
			throws IOException {

		File file = new File(csvPath);
		FileWriter writer = new FileWriter(file, append);

		// print title of evaluation run
		writer.write(experimentTitle + " \n");

		ArrayList<EvaluationResult> results = (ArrayList<EvaluationResult>) lastResults;

		// A map that contains the eval method and a pointer to the results
		// per method
		Map<String, Map<String, Double>> allResults = new HashMap<String, Map<String, Double>>();

		// Go through the results and split up everyting
		for (EvaluationResult r : results) {
			Map<String, Double> resultsPerAlgorithm = allResults.get(r
					.getAlgorithm());
			if (resultsPerAlgorithm == null) {
				resultsPerAlgorithm = new HashMap<String, Double>();
				allResults.put(r.getAlgorithm(), resultsPerAlgorithm);
			}
			resultsPerAlgorithm.put(Utilities101.removePackageQualifiers(r.getMethodName()), r.getValue());

		}

		// print header columns containing the evalmethod
		writer.write("Algorithm;");
		for (String methodname : evalMethods) {
			writer.write(methodname + ";");
		}
		writer.write("\n");

		// print a line for each algorithm in the resultset
		for (String algorithm : allResults.keySet()) {
			Map<String, Double> resultsPerAlgorithm = allResults.get(algorithm);

			writer.write(Utilities101.removePackageQualifiers(algorithm) + ";");

			// print a column for each value of the used evaluation methods
			for (String methodname : evalMethods) {
				writer.write(Recommender101Impl.decimalFormat
						.format(resultsPerAlgorithm.get(methodname)) + ";");
			}

			writer.write("\n");
		}

		writer.write("\n");

		writer.flush();
		writer.close();

	}
	
	/**
	 * Writes and appends _detailed_ results to csv file
	 * 
	 * @param experimentTitle Title as headline for csv output
	 * @param lastDetailedResults Evaluation results
	 * @param csvPath Path to CSV file
	 * @param csvMode 
	 * @throws IOException
	 */
	public static void writeToCSV(String experimentTitle,
			Map<Integer, List<EvaluationResult>> results,
			String csvPath, boolean append,
			List<String> evalMethods) throws IOException {
		File file = new File(csvPath);
		FileWriter writer = new FileWriter(file, append);

		// print title of evaluation run
		writer.write(experimentTitle + " \n");

		// A map that contains the eval method and a pointer to the results
		// per method per split
		Map<String, Map<Integer, Map<String, Double>>> allResults = new HashMap<>();

		// Go through the results and split up everything
		for (Entry<Integer, List<EvaluationResult>> entry : results.entrySet()) {
			int splitNo = entry.getKey();
			for (EvaluationResult evaluationResult : entry.getValue()) {
				Map<Integer, Map<String, Double>> resultPerAlgo = allResults.get(evaluationResult.getAlgorithm());
				if(resultPerAlgo==null){
					resultPerAlgo = new HashMap<Integer, Map<String,Double>>();
					allResults.put(evaluationResult.getAlgorithm(), resultPerAlgo);
				}
				Map<String, Double> splitResultPerAlgo = resultPerAlgo.get(splitNo);
				if(splitResultPerAlgo==null){
					splitResultPerAlgo = new HashMap<String, Double>();
					resultPerAlgo.put(splitNo, splitResultPerAlgo);
				}
				splitResultPerAlgo.put(Utilities101.removePackageQualifiers(evaluationResult.getMethodName()), evaluationResult.getValue());
			}

		}

		// print header columns containing the evalmethod
		writer.write("Algorithm;Split;");
		for (String methodname : evalMethods) {
			writer.write(methodname + ";");
		}
		writer.write("\n");

		// print a line for each algorithm in the resultset
		for (String algorithm : allResults.keySet()) {
			
			Map<Integer, Map<String, Double>> resultsPerAlgorithm = allResults.get(algorithm);

			for (Entry<Integer, Map<String, Double>> split : resultsPerAlgorithm.entrySet()) {
				writer.write(Utilities101.removePackageQualifiers(algorithm) + ";");
				
				writer.write(split.getKey() + ";");

				// print a column for each value of the used evaluation methods
				for (String methodname : evalMethods) {
					writer.write(Recommender101Impl.decimalFormat
							.format(split.getValue().get(methodname)) + ";");
				}
				writer.write("\n");
			}
			
		}

		writer.write("\n");

		writer.flush();
		writer.close();
	}

	/**
	 * Writes and appends runtimes to csv file
	 * @param lastResults Runtime results
	 * @param csvPath Path to CSV file
	 * @throws IOException
	 */
	public static void writeToCSV(String experimentTitle, Map<Integer, List<RuntimeResult>> runtimeResults, String csvPath, boolean append) throws IOException {
		File file = new File(csvPath);
		FileWriter writer = new FileWriter(file, append);

		// print title of evaluation run
		writer.write(experimentTitle + " \n");

		// print header columns containing the evalmethod
		writer.write("EvaulationRound;Algorithm;TrainTime;PredictTime;OverallTime");
		
		writer.write("\n");

		// print a line for each eval round in the resultset
		for(Integer i : runtimeResults.keySet()){
			for(RuntimeResult r : runtimeResults.get(i)){
				writer.write(r.getEvaluationRound() + ";" + Utilities101.removePackageQualifiers(r.getAlgorithm()) + ";" + r.getTrainTime() + ";" +  r.getPredictTime() + ";" + r.getOverallTime());
				writer.write("\n");
			}
		}
		
		writer.write("\n");

		writer.flush();
		writer.close();
		
	}

	

}
