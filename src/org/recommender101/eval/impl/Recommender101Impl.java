/** DJ **/
package org.recommender101.eval.impl;

import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.recommender101.data.DataModel;
import org.recommender101.data.DataSplitter;
import org.recommender101.data.DefaultDataLoader;
import org.recommender101.data.DefaultDataSplitter;
import org.recommender101.eval.interfaces.EvaluationResult;
import org.recommender101.eval.interfaces.RuntimeResult;
import org.recommender101.recommender.AbstractRecommender;
import org.recommender101.tools.CSVOutputWriter;
import org.recommender101.tools.ClassInstantiator;
import org.recommender101.tools.DataSetStatistics;
import org.recommender101.tools.Debug;
import org.recommender101.tools.Utilities101;

/**
 * The central class managing all sorts of stuff
 * 
 * @author DJ
 * 
 */
public class Recommender101Impl {

	public static String VERSION_INFO = "Recommender101 v0.61, 2015-12-09";
	// constants and defaults
	public static float MIN_RATING = 1;
	public static float MAX_RATING = 5;
	public static String csvPath = null;
	public static String csvRuntimePath = null;
	public static boolean csvAppend = false;
	private String csvDetailedPath = null;

	// Default number of threads
	public static int NUM_OF_THREADS = 4;

	/**
	 * A setter to determine when an item is relevant
	 */
	public static int PREDICTION_RELEVANCE_MIN_PERCENTAGE_ABOVE_AVERAGE = 0;

	/**
	 * Determines which rating threshold should be used
	 */
	public static float PREDICTION_RELEVANCE_MIN_RATING_FOR_RELEVANCE = -1;
	
	/**
	 * Should the recommender remove non-relevant items when recommending?
	 *
	 */
	public static boolean FILTER_NON_RELEVANT_ITEMS_FOR_RECOMMENDATION = false; 
	
	/**
	 * path to the csv file to append the results
	 */
	private static String PROP_CSV_PATH = "CSVOutputPath";
	
	/**
	 * path to the csv file to append the _detailed_ results (results for all splits)
	 */
	private static String PROP_CSV_DETAILED_PATH = "CSVDetailedOutputPath";
	
	/**
	 * CSV output mode
	 */
	private static String PROP_CSV_MODE = "CSVOutputMode";
	
	/**
	 * property name of the experiment title
	 */
	private static String PROP_EXPERIMENT_TITLE = "ExperimentTitle";
	
	/**
	 * property name of the experiment title
	 */
	private static String PROP_CSV_RUNTIME_PATH = "CSVRuntimeOutputPath";
	
	/**
	 * title of the evaluation run to be printed in the csv file
	 */
	private String experimentTitle = null;

	/**
	 * The global top n value
	 */
	public static int TOP_N = 10;
	
	public enum evaluationTypes {
		crossvalidation, giventrainingtestsplit
	}

	/**
	 * A handle to the defined properties to be read by everyone
	 */
	public static Properties properties = null;

	/**
	 * A pointer to the data model
	 */
	DataModel dataModel = null;

	/**
	 * A getter for the global data model
	 * 
	 * @return
	 */
	public DataModel getDataModel() {
		return dataModel;
	}
	
	/**
	 * Set the data model.
	 * This can be, for example, used when the data has already been loaded for a repeated run of experiments.
	 */
	public void setDataModel(DataModel dataModel){
		this.dataModel = dataModel;
	}

	/**
	 * The list of recommenders to compare
	 */
	List<AbstractRecommender> recommenders;

	/**
	 * The names of the evaluator classes
	 */
	String evaluators;

	/**
	 * A handle to a (custom or default) data splitter
	 */
	DataSplitter splitter;

	/**
	 * A flag indicating if the (two) splits by the data splitter contain a
	 * given training test split
	 */
	public static boolean givenTrainingTest = false;

	/**
	 * A pointer to the detailed results of the last evaluation round
	 * 
	 */
	Map<Integer, List<EvaluationResult>> lastDetailedResults;

	/**
	 * A pointer to the last (averaged) results calculated in the experiments
	 */
	List<EvaluationResult> lastResults;
	
	/**
	 * Here we store the given n configuration (n/percentage of users)
	 */
	public static String givenNConfiguration = null;

	
	/** The internal crossvalidation runner */
	public CrossValidationRunner runner;
	
	public int nbFolds = -1;
	
	/**
	 * This is to store runtimes per eval round
	 */
	private Map<Integer, List<RuntimeResult>> lastRuntimes;

	
	// =====================================================================================

	/**
	 * A constructor which takes a set of properties
	 * 
	 * @param configuration, a set of properties
	 */
	public Recommender101Impl(Properties configuration) throws Exception {
		properties = configuration;
		init();
	}

	/**
	 * A constructor which accepts the name of a configuration file
	 * 
	 * @param confFileName
	 */
	public Recommender101Impl(String configurationFile) throws Exception {
		Properties props = new Properties();
		props.load(new FileInputStream(configurationFile));
		properties = props;
		init();
	}

	// =====================================================================================

	/**
	 * A default constructor which loads the properties from a standard location
	 * and inits things
	 */
	public Recommender101Impl() throws Exception {
		properties = new Properties();
		properties.load(new FileReader(CONFIGURATION_FILE));
		init();
	}

	// =====================================================================================

	/**
	 * The method initializes things
	 */
	public void init() throws Exception {
		System.out.println(VERSION_INFO);
		System.out.println((new Date()).toString());
		
		DefaultDataLoader dataLoader = null;
		dataLoader = (DefaultDataLoader) ClassInstantiator.instantiateClassByProperty(
				properties, PROP_DATA_LOADER, DefaultDataLoader.class);
		
		
		// Load the data model
		dataModel = (DataModel) ClassInstantiator.instantiateClassByProperty(
				properties, PROP_DATA_MODEL, DataModel.class);

		dataLoader.loadData(dataModel);

		
		
		/**
		 * Print out the statistics at the beginning
		 */
		//DataSetStatistics stats = new DataSetStatistics();
		//stats.printBasicStatistics(getDataModel());

		splitter = (DataSplitter) ClassInstantiator.instantiateClassByProperty(
				properties, PROP_DATA_SPLITTER, DefaultDataSplitter.class);
		
		this.nbFolds = splitter.getNbFolds();

		
		String evaluationType = properties.getProperty(PROP_EVALUATION_TYPE);
		
		// The default
		evaluationTypes evalType = evaluationTypes.crossvalidation;
		if(evaluationType != null){
			evaluationType = evaluationType.trim();
			if (evaluationTypes.giventrainingtestsplit.toString()
					.equalsIgnoreCase(evaluationType)) {
				evalType = evaluationTypes.giventrainingtestsplit;
			}
		}

		givenTrainingTest = false;
		if (evalType == evaluationTypes.giventrainingtestsplit) {
			// Given training test things.
			System.out.println("Given training test");
			givenTrainingTest = true;
		}
		
		recommenders = loadRecommenders();
		evaluators = getEvaluators();

		//Try to load a different number of decimal places for the output of metric results (e.g. "0.1234" instead of "0.123"
		if (properties.getProperty(PROP_GLOBAL_OUPUT_DECIMAL_PLACES) != null){
			try{
				int outputDecimalPlaces = Integer.parseInt(properties.getProperty(PROP_GLOBAL_OUPUT_DECIMAL_PLACES));
				if(outputDecimalPlaces>0){
					StringBuilder df = new StringBuilder("0.0");
					for(int i = 1; i< outputDecimalPlaces; i++){
						df.append("0");
					}
					decimalFormat = new DecimalFormat(df.toString());
				}
			}catch(Exception ex){
				System.out.println("Incorrect config for " + PROP_GLOBAL_OUPUT_DECIMAL_PLACES + ". Has to be positive integer. Will continue with default.");
			}
		}
		
		// Check if csv output should be used
		csvPath = properties.getProperty(PROP_CSV_PATH);
		
		// Check if detailed csv output should be used
		csvDetailedPath = properties.getProperty(PROP_CSV_DETAILED_PATH);
		
		// Check if csv mode append or overwrite should be used
		if (properties.getProperty(PROP_CSV_MODE) != null &&
				properties.getProperty(PROP_CSV_MODE).equals("append"))
			csvAppend = true;
		
		// Title of this run for csv output
		experimentTitle = properties.getProperty(PROP_EXPERIMENT_TITLE);
		if (experimentTitle == null) {
			experimentTitle = "Experiment results";
		}
		
		// Check if csv output should be used
		csvRuntimePath = properties.getProperty(PROP_CSV_RUNTIME_PATH);

		/**
		 * Read the property values
		 */
		readProperty("PROP_GLOBAL_NUM_OF_THREADS", "NUM_OF_THREADS");
		readProperty("PROP_GLOBAL_MAX_RATING", "MAX_RATING");
		readProperty("PROP_GLOBAL_MIN_RATING", "MIN_RATING");
		readProperty("PROP_GLOBAL_GIVEN_N_CONFIGURATION", "givenNConfiguration");
		readProperty("PROP_GLOBAL_TOP_N", "TOP_N");
		readProperty("PROP_GLOBAL_FILTER_NON_RELEVANT_ITEMS_FOR_RECOMMENDATION", "FILTER_NON_RELEVANT_ITEMS_FOR_RECOMMENDATION");
		readProperty("PROP_GLOBAL_PREDICTION_RELEVANCE_MIN_PERCENTAGE_ABOVE_AVERAGE", "PREDICTION_RELEVANCE_MIN_PERCENTAGE_ABOVE_AVERAGE");
		readProperty("PROP_GLOBAL_PREDICTION_RELEVANCE_MIN_RATING", "PREDICTION_RELEVANCE_MIN_RATING_FOR_RELEVANCE");

		// More settings
		dataModel.setMaxRatingValue(MAX_RATING);
		dataModel.setMinRatingValue(MIN_RATING);
		
		/**
		 * Print out the statistics at the end
		 */
		DataSetStatistics stats = new DataSetStatistics();
		stats.printBasicStatistics(getDataModel());
	}

	// =====================================================================================

	/**
	 * Runs the experiments whose descriptions have been loaded in the init()
	 * phase. We use the CrossValidationRunner both for cross-validation and
	 * given training/test splits
	 * 
	 * @throws Exception
	 */
	public void runExperiments() throws Exception {
		runner = new CrossValidationRunner(dataModel, recommenders, evaluators, splitter, givenTrainingTest);
		lastDetailedResults = runner.runExperiments();
		lastResults = calcualteAverageResults(lastDetailedResults);
		lastRuntimes = runner.getRuntimeResults();
		if ( csvPath != null){
			Debug.log("Appending the results to csv file");
			try  {
				CSVOutputWriter.writeToCSV(experimentTitle, lastResults, csvPath, csvAppend, Utilities101.getEvaluatorList(evaluators));
			}
			catch (Exception e) {
				System.err.println("[Error] Writing to file " + csvPath + " failed: " + e.getMessage());
			}
			
		}
		
		if (csvDetailedPath != null){
			Debug.log("Appending the detailed results to csv file");
			try  {
				CSVOutputWriter.writeToCSV(experimentTitle, lastDetailedResults, csvDetailedPath, csvAppend, Utilities101.getEvaluatorList(evaluators));
			}
			catch (Exception e) {
				System.err.println("[Error] Writing to file " + csvDetailedPath + " failed: " + e.getMessage());
			}
		}
		
		if (csvRuntimePath != null){
			Debug.log("Appending the runtimes to csv file");
			try  {
				CSVOutputWriter.writeToCSV(experimentTitle, lastRuntimes, csvRuntimePath, csvAppend);
			}
			catch (Exception e) {
				System.err.println("[Error] Writing to file " + csvRuntimePath + " failed: " + e.getMessage());
			}
		}
		
			
	}

	// =====================================================================================

	/**
	 * A method that computes the average value of all cross-validation results.
	 * 
	 * 
	 * @param resultsPerEvalRun
	 * @return the EvaluationResult list with averaged values
	 */
	public List<EvaluationResult> calcualteAverageResults(Map<Integer, List<EvaluationResult>> resultsPerEvalRun) throws Exception {
		List<EvaluationResult> result = new ArrayList<EvaluationResult>();

		// Go through some set of results and see what algorithms and
		// evaluations we have
		Set<String> recommenderStrings = new HashSet<String>();
		Set<String> evalStrings = new HashSet<String>();

		List<Integer> keyList = new ArrayList<Integer>(resultsPerEvalRun.keySet());
		List<EvaluationResult> firstResult = resultsPerEvalRun.get(keyList.get(0));

		for (EvaluationResult r : firstResult) {
			recommenderStrings.add(r.getAlgorithm());
			evalStrings.add(r.getMethodName());
		}

		
		// Create an evaluation result for everything that we expect
		for (String rec : recommenderStrings) {
			for (String ev : evalStrings) {
				result.add(new EvaluationResult(rec, ev, Double.NaN));
			}
		}

		// Now go through the evaluation rounds and aggregate the results
		for (Integer round : resultsPerEvalRun.keySet()) {
			List<EvaluationResult> resultsOfRound = resultsPerEvalRun.get(round);
			for (EvaluationResult aResult : resultsOfRound) {

				if (Double.isNaN(aResult.getValue())) {
					Debug.log("NaN value for the following round: "
							+ aResult.getAlgorithm() + ":"
							+ aResult.getMethodName());
				}
				addResultOfRound(result, aResult);
			}
		}
		// Now compute the average value
		for (EvaluationResult finalResult : result) {
					finalResult.setValue(finalResult.getValue() / resultsPerEvalRun.keySet().size());
		}

		return result;
	}

	/**
	 * Adds the evaluationresult to the existing results
	 * 
	 * @param resultsSoFar
	 * @param aResult
	 */
	void addResultOfRound(List<EvaluationResult> resultsSoFar,
			EvaluationResult aResult) throws Exception {
		// find the correct entry
		EvaluationResult existingResult = null;
		for (EvaluationResult res : resultsSoFar) {
			if (res.equals(aResult)) {
				existingResult = res;
				break;
			}
		}
		// Set the values.
		if (existingResult == null) {
			throw new Exception(
					"Recommender101.averageResult: Cannot find Evalution Result for "
							+ aResult.getAlgorithm() + ":\n"
							+ aResult.getMethodName());
		}
		if (Double.isNaN(existingResult.getValue())) {
			existingResult.setValue(aResult.getValue());
		} else {
			existingResult.setValue(existingResult.getValue()
					+ aResult.getValue());
		}
	}

	// =====================================================================================

	/**
	 * Test only to print the evaluation results. To be extended to use the
	 * algorithm classes themselves and a more detailed result presentation
	 * 
	 * @param results
	 */
	public void printExperimentResults(List<EvaluationResult> results) {
		System.out.println("-------------------------------------------");
		System.out.println("Results of the evaluation :");
		for (EvaluationResult result : results) {
			String algorithm = result.getAlgorithm();
			String evalMethod = result.getMethodName();
			double value = result.getValue();

			algorithm = Utilities101.removePackageQualifiers(algorithm);
			evalMethod = Utilities101.removePackageQualifiers(evalMethod);

			System.out.format("%-32s%-20s%-8s", algorithm, evalMethod,
					decimalFormat.format(value));
			System.out.println();

		}
		System.out.println("-------------------------------------------");
	}



	// =====================================================================================

	/**
	 * Test -> print results of evaluation arranged per metric in descending
	 * order
	 * 
	 * @param results
	 */
	public void printSortedEvaluationResults(List<EvaluationResult> results) {

		// A map that contains the eval method and a pointer to the results per
		// method
		Map<String, Map<String, Double>> allResults = new HashMap<String, Map<String, Double>>();

		// Go through the results and split up everything
		for (EvaluationResult r : results) {
			Map<String, Double> resultsPerMethod = allResults.get(r.getMethodName());
			if (resultsPerMethod == null) {
				resultsPerMethod = new HashMap<String, Double>();
				allResults.put(r.getMethodName(), resultsPerMethod);
			}
			resultsPerMethod.put(r.getAlgorithm(), r.getValue());
		}
		
		// sort the results alphabetically descending based on the name of the metric
		List<SimpleEntry<String, Map<String, Double>>> allResultsAlphabetical = new ArrayList<SimpleEntry<String, Map<String, Double>>>();
		for (Map.Entry<String, Map<String, Double>> entry : allResults.entrySet()){
			SimpleEntry<String, Map<String, Double>> newEntry = new SimpleEntry<String, Map<String, Double>>(
					Utilities101.removePackageQualifiers(entry.getKey()),
					entry.getValue());
			boolean notAddedYet = true;
			for(int i = 0; notAddedYet && i < allResultsAlphabetical.size(); i++){
				SimpleEntry<String, Map<String, Double>> presentEntry = allResultsAlphabetical.get(i);
				if(newEntry.getKey().compareTo(presentEntry.getKey()) < 0){
					allResultsAlphabetical.add(i, newEntry);
					notAddedYet = false;
				}
			}
			if (notAddedYet == true){
				allResultsAlphabetical.add(newEntry);
			}
		}
		
		System.out.println("--------------------------");
		System.out.println("Evaluation results: ");
		System.out.println("--------------------------");
		
		// Go through the different hashmaps and print the results
		for (SimpleEntry<String, Map<String, Double>> entry : allResultsAlphabetical){
			String evalMethod = entry.getKey();
			Map<String, Double> resultsPerMethod = entry.getValue();
			resultsPerMethod = Utilities101.sortByValueDescending(resultsPerMethod);
			for (String algorithm : resultsPerMethod.keySet()) {
				double value = resultsPerMethod.get(algorithm);

				algorithm = Utilities101.removePackageQualifiers(algorithm);
				evalMethod = Utilities101.removePackageQualifiers(evalMethod);

				System.out.format("%-30s |%8s |%-80s", evalMethod,
						decimalFormat.format(value), algorithm);
				System.out.println();
			}
			System.out.println();
		}
		System.out.println("--------------------------");
	}
	
	public void printRuntimeResults(List<RuntimeResult> runtimeResult) {
		System.out.println("Runtime results: ");
		System.out.println("--------------------------");
		
		for(RuntimeResult r : runtimeResult){
			String algorithm = Utilities101.removePackageQualifiers(r.getAlgorithm());
			System.out.format("Training:%10s |Predicting:%10s |%-80s", decimalFormat.format(r.getTrainTime()), decimalFormat.format(r.getPredictTime()), algorithm);
			System.out.println();
		}
		
	} 

	// =====================================================================================
	/**
	 * Loads and instantiates the recommenders from the configuration file 
	 * @return
	 */
	public List<AbstractRecommender> loadRecommenders() throws Exception {
		List<AbstractRecommender> result = new ArrayList<AbstractRecommender>();
		String algoString = properties.getProperty(PROP_ALGORITHM_DESCRIPTIONS);

		if (algoString == null) {
			throw new Exception("No algorithms defined, property: "
					+ PROP_ALGORITHM_DESCRIPTIONS);
		}
		
		// From here till the end of the method: Modified by (timkraemer) 17.09.2012
		// Get the number of validation rounds 
		int roundsCount = 0;
		String maxValRoundsStr = Recommender101Impl.properties
				.getProperty("Debug.MaxValidationRounds");
		if (maxValRoundsStr != null) {
			int rounds = Integer.parseInt(maxValRoundsStr);
			if (rounds > 0) {
				roundsCount = rounds;
			}
		}
		
		if (roundsCount == 0) {
			roundsCount = this.nbFolds;
		}
		
		// If we have only two data splits which were given, only do the first iteration
			if (givenTrainingTest) {
				roundsCount = 1;
			}
		//System.out.println("Num of validation rounds: "+roundsCount);
		
		// The old version
		// result = ClassInstantiator.instantiateClassesByProperties(algoString);
		// Now result has to contain enough independent recommender objects in order to solve multithreading issues
		for (int i=0; i<roundsCount; i++)
			for (Object o : ClassInstantiator.instantiateClassesByProperties(algoString))
				result.add((AbstractRecommender)o);
		
		//System.out.println("Result length: "+result.size());
		return result;
	}

	// =====================================================================================

	/**
	 * Get the evaluator strings from the property files. Have to be
	 * instantiated later on..
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getEvaluators() throws Exception {
		String result;
		String metricsString = properties
				.getProperty(PROP_EVALUATOR_DESCRIPTIONS);
		if (metricsString == null) {
			throw new Exception("No metrics defined, property: "
					+ PROP_EVALUATOR_DESCRIPTIONS);
		} else {
			result = metricsString;
		}
		return result;
	}

	// =====================================================================================

	/**
	 * Returns the results of the last experiment run
	 * 
	 * @return
	 */
	public Map<Integer, List<EvaluationResult>> getLastDetailedResults() {
		return lastDetailedResults;
	}
	
	/**
	 * Returns the last measured runtimes of the algorithms
	 * 
	 * @return
	 */
	public Map<Integer, List<RuntimeResult>> getLastDetailedRuntimes() {
		return lastRuntimes;
	}
	
	/**
	 * Returns the averaged results runtimes
	 * 
	 * @return
	 */
	public List<RuntimeResult> getLastRuntimes() {
		List<RuntimeResult> results = new ArrayList<>();
		HashMap<String, List<RuntimeResult>> resultsByAlgorithm = new HashMap<>();
		
		for(Integer i :lastRuntimes.keySet()){
			for(RuntimeResult r : lastRuntimes.get(i)){
				if(!resultsByAlgorithm.containsKey(r.getAlgorithm()))
					resultsByAlgorithm.put(r.getAlgorithm(), new ArrayList<RuntimeResult>());
				resultsByAlgorithm.get(r.getAlgorithm()).add(r);
			}
		}
		
		for(String s :resultsByAlgorithm.keySet()){
			RuntimeResult averagedResult = new RuntimeResult(s);
			long trainTime = 0;
			long testTime = 0;
			for(RuntimeResult r : resultsByAlgorithm.get(s)){
				trainTime += r.getTrainTime();
				testTime += r.getPredictTime();
			}
			averagedResult.setTrainTime(trainTime/resultsByAlgorithm.get(s).size());
			averagedResult.setPredictTime(testTime/resultsByAlgorithm.get(s).size());
			results.add(averagedResult);
		}
		return results;
	}

	/**
	 * Returns the averaged results
	 * 
	 * @return
	 */
	public List<EvaluationResult> getLastResults() {
		return lastResults;
	}

	
	// THE CONSTANTS
	// Default location of property file
	public static String CONFIGURATION_FILE = "conf/recommender101.properties";

	// Property name for data loader
	public static String PROP_DATA_LOADER = "DataLoaderClass";
	// Property name for data splitter
	public static String PROP_DATA_SPLITTER = "DataSplitterClass";
	// Property name for algorithm loader
	public static String PROP_ALGORITHM_DESCRIPTIONS = "AlgorithmClasses";
	// Property name for evaluators
	public static String PROP_EVALUATOR_DESCRIPTIONS = "Metrics";
	// property name for data models
	public static String PROP_DATA_MODEL = "DataModelClass";
	// property name for data models
	public static String PROP_EVALUATION_TYPE = "EvaluationType";

	// The minimum rating
	public static String PROP_GLOBAL_MIN_RATING = "GlobalSettings.minRating";
	// the max rating
	public static String PROP_GLOBAL_MAX_RATING = "GlobalSettings.maxRating";
	// the global list length default
	public static String PROP_GLOBAL_TOP_N= "GlobalSettings.topN";

	// threshold for relevance for list metrics
	public static String PROP_GLOBAL_PREDICTION_RELEVANCE_MIN_PERCENTAGE_ABOVE_AVERAGE = "GlobalSettings.listMetricsRelevanceMinPercentageAboveAverage";
	// threshold for min rating for relevance for list metrics
	public static String PROP_GLOBAL_PREDICTION_RELEVANCE_MIN_RATING = "GlobalSettings.listMetricsRelevanceMinRating";
	// filtering of items
	public static String PROP_GLOBAL_FILTER_NON_RELEVANT_ITEMS_FOR_RECOMMENDATION = "GlobalSettings.filterNonRelevantItemsForRecommendation";

	// Number of threads to use
	public static String PROP_GLOBAL_NUM_OF_THREADS = "GlobalSettings.numOfThreads";

	// The given-n configuration string
	public static String PROP_GLOBAL_GIVEN_N_CONFIGURATION = "GlobalSettings.givenNConfiguration";
	
	// The given-n configuration string
	public static String PROP_GLOBAL_OUPUT_DECIMAL_PLACES = "GlobalSettings.outputDecimalPlaces";

	public static DecimalFormat decimalFormat = new DecimalFormat("#.###");
		
	/**
	 * Sets s a property based on the field
	 * @param field
	 */
	public void readProperty(String fieldname, String fieldToStore) {
		try {
			// Get the setting from the property file
			Field f1 = Recommender101Impl.class.getDeclaredField(fieldname);
			String key = f1.get(this).toString();
			String propertyValue = properties.getProperty(key);
			if (propertyValue != null) {
				Debug.log("Recommender101Impl: readProperty : " + key + " = " + propertyValue);
			}

			// set the field if there is a value
			if (propertyValue != null) {
				propertyValue = propertyValue.trim();
				Field f2 = Recommender101Impl.class.getDeclaredField(fieldToStore);
				if (f2.getType().toString().equals("int")) {
					f2.setInt(this, Integer.parseInt(propertyValue));
				}
				else if (f2.getType().toString().equals("class java.lang.String")) {
					f2.set(this, propertyValue);
				}
				else if (f2.getType().toString().equals("boolean")) {
					f2.set(this, Boolean.parseBoolean(propertyValue));
				}
			}
		} catch (Exception e) {
			System.err.println("[FATAL:] Cannot set property field " + fieldname);
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}

}
