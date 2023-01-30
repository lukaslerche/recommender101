package org.recommender101.recommender.extensions.jfm;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import org.recommender101.gui.annotations.R101Class;
import org.recommender101.gui.annotations.R101Setting;
import org.recommender101.gui.annotations.R101Setting.SettingsType;
import org.recommender101.recommender.AbstractRecommender;
import org.recommender101.recommender.extensions.jfm.impl.FactorizationMachine;
import org.recommender101.recommender.extensions.jfm.impl.FactorizationMachine.FactorizationMethod;
import org.recommender101.recommender.extensions.jfm.impl.TaskType;

/**
 * This class is used to create a recommender instance, that uses the factorization machines approach by Rendle et. al. to predict ratings.
 * @author Michael Jugovac
 */
@SuppressWarnings("serial")
@R101Class (name="Factorization Machine", description="Implements the factorization approach created by Rendle et. al.")
public class LibFmRecommender extends AbstractRecommender {

    private FactorizationMachine _fm;
    FactorizationMachine.FactorizationMethod _method= FactorizationMethod.SGDA;
    private double _initStdev = 0.1;
    private int[] _dim = new int[]{1,1,8};
    private boolean _doSampling = true;
    private boolean _doMultilevel = true;
    private int _numIter = 100;
    private int _numEvalCases = 100;
    private TaskType _taskType = TaskType.Regression;
    private double[] _regular = new double[]{0,0,0};
    private double[] _learnRates = new double[] {0.01};
    private boolean _verbose = false;
	private boolean _contextEnabled = true;
	private HashMap<Integer, HashMap<Integer, int[]>> _contextSourceForTestData;
    
    @Override
    /**
     *	Delegates to FactorizationMachine. For more details refer to documentation there.
     */
    public float predictRating(int user, int item) {
        return _fm.PredictRating(user, item);
    }

    @Override
    /**
     * Uses the default approach to create recommendations for a user
     */
    public List<Integer> recommendItems(int user) {
        return super.recommendItemsByRatingPrediction(user);
    }

    @Override
    /**
     * Initializes the data structures of libfm and starts the learning process. More detailed descriptions can be found in the documentation of FactorizationMachine.
     */
    public void init() throws Exception {
        _fm = new FactorizationMachine();
        _fm.Initialize(_method, _initStdev, _dim, _doSampling, _doMultilevel, _numIter, _numEvalCases, _taskType, _regular, _learnRates, _verbose, dataModel, _contextEnabled, _contextSourceForTestData);
        _fm.Learn();
    }
    
    @R101Setting(displayName="Learning Method", description="Learning Method. 0 = SGD, 1 = SGDA, 2 = MCMC, 3 = ALS",
                    type=SettingsType.INTEGER, defaultValue="1", minValue=0, maxValue=3)
    /**
     * Sets the learning method of libfm, which is basically the algorithm that is to be used.
     * @param method The method to be set.
     */
    public void setMethod(String method) {
            int temp = Integer.parseInt(method);
            switch(temp){
                case 0:
                    _method = FactorizationMachine.FactorizationMethod.SGD;
                    break;
                case 1:
                    _method = FactorizationMachine.FactorizationMethod.SGDA;
                    break;
                case 2:
                    _method = FactorizationMachine.FactorizationMethod.MCMC;
                    break;
                case 3:
                    _method = FactorizationMachine.FactorizationMethod.ALS;
                    break;
            }
    }
    
    @R101Setting(displayName="Init Stdev", description="The init value for the stdev parameter",
                    type=SettingsType.DOUBLE, defaultValue="0.1", minValue=0)
    /**
     * Set the standard deviation that is to be used for the random initialization values of the prediction. Some algorithms react strongly to this value according to the paper.
     * @param initStdev The standard deviation to be set
     */
    public void setInitStdev(String initStdev) {
            _initStdev = Double.parseDouble(initStdev);
    }
    
    @R101Setting(displayName="dim", description="'k0,k1,k2': k0=use bias, k1=use 1-way interactions, k2=dim of 2-way interactions",
                    type=SettingsType.TEXT, defaultValue="1,1,8")
    /**
     * Controls how regularization parameters are to be used.
     * @param dim Refer to paper or manual for values. Splitting of values with "#".
     */
    public void setDim(String dim) {
        String[] temp = dim.split("#");
        _dim = new int[temp.length];
        for(int i = 0 ; i < temp.length;i++){
            _dim[i] = Integer.parseInt(temp[i]);
        }   
    }
    
    @R101Setting(displayName="Do Sampling", description="Enables sampling",
			defaultValue="true", type=SettingsType.BOOLEAN)
    /**
     * Sets if sampling is to be used by the learning algorithm. Only valid for some algorithms. Can be safely omitted in most cases.
     * @param doSampling The value for the sampling parameter to be set. 
     */
    public void setDoSampling(String doSampling) {
            if ("true".equalsIgnoreCase(doSampling)) {
                    _doSampling = true;
            }
    }
    
    @R101Setting(displayName="Do Multilevel", description="Enables multilevel",
			defaultValue="true", type=SettingsType.BOOLEAN)
    /**
     * Sets if multilevel is to be used by the learning algorithm. Only valid for some algorithms. Can be safely omitted in most cases.
     * @param doMultilevel The value for the multilevel parameter to be set. 
     */
    public void setDoMultilevel(String doMultilevel) {
            if ("true".equalsIgnoreCase(doMultilevel)) {
                    _doMultilevel = true;
            }
    }
    
    
    @R101Setting(displayName="Context Enabled", description="Enables context usage",
	            type=SettingsType.INTEGER, defaultValue="true")
    /**
     * If set to false the context will be ignored even if it is provided by the data model object.
     * @param contextEnabled The value that represents if the context is to be enabled.
     */
	public void setContextEnabled(String contextEnabled) {
	    _contextEnabled = contextEnabled.toLowerCase().equals("true");
	}
    
    @SuppressWarnings("unchecked")
	@R101Setting(displayName="Context source for test data", description="Fully qualified name of a globally visible static HashMap<int, HashMap<int, int[]>",
            type=SettingsType.INTEGER, defaultValue="true")
	/**
	 * This method works via reflection. It accepts a String name for a global static field of type HashMap<Interger, HashMap<Integer, int[]> (e.g. "org.exmpl.ContextDataLoader.globalContextInfo"). It is used, because no context info can currently be passed to the recommender imlementation via the predict method.
	 * @param A string that represents the fully qualified name of a global static field variable containing context info.
	 */
	public void setContextSourceForTestData(String contextSourceForTestData) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	String[] splitName = contextSourceForTestData.split("\\.");
    	String className = "";
    	for(int i=0; i < splitName.length - 1 /*cut off the field name*/;i++){
    		className += splitName[i];
    		if(i!=splitName.length-2)
    			className += ".";
    	}
    	///First get the class
    	Class<?> globalClass = Class.forName(className);
    	///Then the field
    	Field field = globalClass.getField(splitName[splitName.length-1]);
    	///Then the fields value. Null is passed as a parameter, because this is not an instance type but rather a static field
    	Object o = field.get(null);
    	_contextSourceForTestData = (HashMap<Integer, HashMap<Integer,int[]>>)o;
	}
    
    @R101Setting(displayName="Number of Iterations", description="Number of Iterations for the learning algorithm",
                    type=SettingsType.INTEGER, defaultValue="100")
    /**
     * Sets the number of iterations for the learning algorithm.
     * @param numIter The number of iterations to be set.
     */
    public void setNumIter(String numIter) {
            _numIter = Integer.parseInt(numIter);
    }
    
    @R101Setting(displayName="Number of EvalCases", description="Number of EvalCases for the learning algorithm",
                    type=SettingsType.INTEGER, defaultValue="100")
    /**
     * Sets the number of iterations for the evaluation algorithm. Can be safely omitted.
     * @param numIter The number of iterations to be set.
     */
    public void setEvalCases(String numEvalCases) {
            _numEvalCases = Integer.parseInt(numEvalCases);
    }
    
    @R101Setting(displayName="Task Type", description="Task Type. 0 = Regression, 1 = Classification",
                    type=SettingsType.INTEGER, defaultValue="0", minValue=0, maxValue=1)
    /**
     * Sets the task type. To date only Regression makes sense, but Classification might be interesting later on, so the paramter is kept, but it may be safely omitted.
     * @param taskType The task type to be set.
     */
    public void setTaskType(String taskType) {
            int temp = Integer.parseInt(taskType);
            switch(temp){
                case 0:
                    _taskType = TaskType.Regression;
                    break;
                case 1:
                    _taskType = TaskType.Classification;
                    break;
            }
    }
    
    @R101Setting(displayName="Regular", description="'r0,r1,r2' for SGD and ALS: r0=bias regularization, r1=1-way regularization, r2=2-way regularization",
                    type=SettingsType.TEXT, defaultValue="0#0#0")
    /**
     * Sets the regularization values. Some algorithms react very strongly to this parameter. Refer to the paper for more information.
     * @param regular The regularization values to be set. Split by "#". Can also be only one value by itself.
     */
    public void setRegular(String regular) {
        String[] temp = regular.split("#");
        _regular = new double[temp.length];
        for(int i = 0 ; i < temp.length;i++){
            _regular[i] = Double.parseDouble(temp[i]);
        }   
    }
    
    @R101Setting(displayName="Learn Rates", description="learn_rate for SGD (default=0.01)",
                    type=SettingsType.TEXT, defaultValue="0.01")
    /**
     * Sets the learn rate values. Some algorithms react very strongly to this parameter. Refer to the paper for more information.
     * @param learnRates The learn rate values to be set. Split by "#". Can also be only one value by itself.
     */
    public void setLearnRates(String learnRates) {
        String[] temp = learnRates.split("#");
        _learnRates = new double[temp.length];
        for(int i = 0 ; i < temp.length;i++){
            _learnRates[i] = Double.parseDouble(temp[i]);
        }   
    }
    
    @R101Setting(displayName="Verbose", description="Enables verbose logging.",
			defaultValue="true", type=SettingsType.BOOLEAN)
    /**
     * Sets the verbosity for the output. If true, more output is generated.
     * @param verbose The verbosity to be set.
     */
    public void setVerbose(String verbose) {
            if ("true".equalsIgnoreCase(verbose)) {
                    _verbose = true;
            }
    }
    
}
