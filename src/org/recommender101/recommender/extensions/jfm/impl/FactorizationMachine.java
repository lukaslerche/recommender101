/*
	libFM: Factorization Machines

	Based on the publication(s):
	* Steffen Rendle (2010): Factorization Machines, in Proceedings of the 10th IEEE International Conference on Data Mining (ICDM 2010), Sydney, Australia.
	* Steffen Rendle, Zeno Gantner, Christoph Freudenthaler, Lars Schmidt-Thieme (2011): Fast Context-aware Recommendations with Factorization Machines, in Proceedings of the 34th international ACM SIGIR conference on Research and development in information retrieval (SIGIR 2011), Beijing, China.
	* Christoph Freudenthaler, Lars Schmidt-Thieme, Steffen Rendle (2011): Bayesian Factorization Machines, in NIPS Workshop on Sparse Representation and Low-rank Approximation (NIPS-WS 2011), Spain.
	* Steffen Rendle (2012): Learning Recommender Systems with Adaptive Regularization, in Proceedings of the 5th ACM International Conference on Web Search and Data Mining (WSDM 2012), Seattle, USA.  
	* Steffen Rendle (2012): Factorization Machines with libFM, ACM Transactions on Intelligent Systems and Technology (TIST 2012).

	Author:   Steffen Rendle, http://www.libfm.org/
	modified: 2012-12-27

	Copyright 2010-2012 Steffen Rendle, see license.txt for more information
*/
package org.recommender101.recommender.extensions.jfm.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.recommender101.data.DataModel;
import org.recommender101.data.DefaultDataSplitter;
import org.recommender101.data.Rating;

/**
 *	This class represents a parameterized instance of a factorization machine. It contains references to all the necessary input data, id mappings, an the algorithm classes that calculate the actual results.
 * @author Michael Jugovac (Port)
 */
public class FactorizationMachine {
	///A reference to the algorithm class. The actual subclass may vary depending on the parameters
    private fm_learn _fml;
    /// The training data
    private Data _train;
    ///The test data
    private Data _test;
    ///An enum that determines which algorithm is currently in use
    private FactorizationMethod _method;
    ///A mapping for R101-ids to libfm-ids. See Data.loadFromR101DataModel for further description on this structure
    private List<HashMap<Integer, Integer>> _idMap;
    ///Switch to false to ignore context data even if it is provided
	private boolean _contextEnabled;
	private HashMap<Integer, HashMap<Integer, int[]>> _globalExtraInfo;
	private float _minRating;
	private float _maxRating;
    
    /**
     * An enum containing the algorithms that libfm offers.
     * @author Michael Jugovac
     *
     */
    public enum FactorizationMethod {
        SGD,
        SGDA,
        ALS,
        MCMC
    }
    
    
    /**
     * This method initializes all data structures that are needed for a run of the factorization machine. The input data is taken from a R101 DataModel instance.
     * @param method The learning method to be used 
     * @param initStdev stdev for initialization of 2-way factors (default=0.1)
     * @param dim 'k0,k1,k2': k0=use bias, k1=use 1-way interactions, k2=dim of 2-way interactions (default=1,1,8)
     * @param doSampling (default=true)
     * @param doMultilevel (default=true)
     * @param numIter number of iterations (default=100) (default fallback for negative values)
     * @param numEvalCases undocumented (default fallback for negative values)
     * @param task task type (regression or binary classification)
     * @param regular 'r0,r1,r2' for SGD and ALS: r0=bias regularization, r1=1-way regularization, r2=2-way regularization
     * @param learnRates learn_rate for SGD (default=0.1)
     * @param debugVerbosity (prints a little more info if set to true
     * @param dataModel A R101 DataModel containing the input data. For details regarding the conversion to libfm structures see the method Data.loadFromR101DataModel
     * @param contextEnabled If false, context will be ignored even if it is provided in the data model
     * @param globalContextData A reference to the context data to be used later on in the prediction phase
     * @throws IOException
     */
    public void Initialize(FactorizationMethod method, 
    					   double initStdev,
    					   int[] dim/*length = 3*/,
    					   boolean doSampling /*true = default*/,
    					   boolean doMultilevel /*true = default*/,
    					   int numIter /*100 = default*/,
    					   int numEvalCases/*test.num_cases = default*/,
    					   TaskType task,
    					   double[] regular/*length = 0||1||3*/,
    					   double[] learnRates,
    					   boolean debugVerbosity,
    					   DataModel dataModel,
    					   boolean contextEnabled,
    					   HashMap<Integer, HashMap<Integer, int[]>> globalContextData) throws IOException{
    	
    	_contextEnabled = contextEnabled;
    	_globalExtraInfo = globalContextData;
        ///Everything was in a try block in the original -> not needed as this is not a command line interface
        
    	///The following code in this method will mostly test, if parameters are set and within allowed ranges. If they are not set, default values will be set.
    	
        if (method==null)  
            method = FactorizationMethod.MCMC; 
        if (dim==null || dim.length != 3) 
            dim = new int[]{1,1,8};
              
        _minRating = dataModel.getMinRatingValue();
        _maxRating = dataModel.getMaxRatingValue();
        
        if (method==FactorizationMethod.ALS) { // als is an mcmc without sampling and hyperparameter inference
                method = FactorizationMethod.MCMC;
                doSampling = false;
                doMultilevel = false;
        } 
        DataModel calib = null;
        if (method == FactorizationMethod.SGDA) {
        	///SGDA needs a validation set for automatic parameter adjustment.
        	///Therefore the first thing to be done is to cut of a fifth of the training data and put it into the calib variable (which is used for validation)
        	calib = new DataModel();
        	DataModel newDataModel = new DataModel();
        	
        	///Using the default data splitter for splitting so that no items or users may be lost in the calib data
        	DefaultDataSplitter splitter = new DefaultDataSplitter();
        	splitter.setNbFolds("5");
        	try {
				List<Set<Rating>> folds = splitter.splitData(dataModel);
				for(int i = 0; i<5; i++){
					DataModel dest = (i==4) ? calib : newDataModel;
					Iterator<Rating> ratingIt = folds.get(i).iterator();
					while(ratingIt.hasNext()){
						Rating r = ratingIt.next();
						dest.addRating(r);
						dest.addExtraInformation(r, dataModel.getExtraInformation(r));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
        	///Training data is now only 4/5th its previous size. The rest is in the calib data used for parameter adjustment.
        	dataModel = newDataModel;
        }

        // (1) Load the data
        Logging.log("Loading train...\t");
        Data train = new Data(
                method!=FactorizationMethod.MCMC, // no original data for mcmc
                ! (method==FactorizationMethod.SGD|| method==FactorizationMethod.SGDA) // no transpose data for sgd, sgda
        );
        ///The id mapping is created and later handed over to the load method, to be filled with mappings of R101-ids to libfm-ids
        _idMap = new ArrayList<>();
        ///Two HashMaps are initially created. One for the users and one for the items. More might be added, if the load function detects context columns in the data model.
        _idMap.add(new HashMap<Integer, Integer>());
        _idMap.add(new HashMap<Integer, Integer>());
        ///The current highest id is set to 0. For each new feature (user,item,context,...) this value is incremented. The increment happens inside the load method. This is the reason why it returns the new idOffset afterwards.
        int idOffset = 0;

        ///The training data is loaded.
        idOffset = train.loadFromR101DataModel(dataModel, _idMap, idOffset, contextEnabled);
        ///Some output follows if verbosity is activated.
        if (debugVerbosity) { train.debug(); }

        Logging.log("Loading test...\t");
        
        ///IMPORTANT:
        ///The test data is created and loaded just like the training data. This is legacy. LibFM wants to work on the test data while it trains (e.g. calculate RMSE). 
        ///Therefore an empty test set is created in the following lines of code. Every method that uses the test data does nothing this way. This approach was easier than to remove the test data completely. It also preserves the possiblity of a run via the main method in this class, which works like it would in the real libfm and can therefore be used for debugging purposes.
        ///booleans for transposition are hard coded because we never want to transpose
        Data test = new Data(
        		true, // no original data for mcmc
                false // no transpose data for sgd, sgda
        );
      
        
        DataModel emptyTestDataModel = new DataModel();
        
        idOffset = test.loadFromR101DataModel(emptyTestDataModel, _idMap, idOffset, contextEnabled);
        if (debugVerbosity) { test.debug(); }
        ///Although there is no data here, this has to be done to satisfy the training methods
        test.create_data_t();

        ///If the algorithm SGDA is used the before created calib data model is now used to create a validation set to enable the self adjusting capabilities of the algorithm
        ///The instantiation of the data structures is analog to the training data set
        Data validation = null;
	    if (method == FactorizationMethod.SGDA) {
	
	            Logging.log("Loading validation set...\t");
	            validation = new Data(
	            		method!=FactorizationMethod.MCMC, // no original data for mcmc
	                    ! (method==FactorizationMethod.SGD|| method==FactorizationMethod.SGDA) // no transpose data for sgd, sgda
	            );
	            
	            validation.loadFromR101DataModel(calib, _idMap, idOffset, contextEnabled);
	            if (debugVerbosity) { test.debug(); }
	    }

	    
        // (1.3) Load meta data
        Logging.log("Loading meta data...\t");

        // (main table)
        int num_all_attribute = Math.max(train.num_feature, test.num_feature);
        if (method == FactorizationMethod.SGDA) {
                num_all_attribute = Math.max(num_all_attribute,  validation.num_feature);
        }
        DataMetaInfo meta = new DataMetaInfo(num_all_attribute);

        ///In this line the original tried to load a meta info file if provided, which can be used, if grouping information regarding the input data is available.
        ///As this is not the case for R101, the code has been removed.

        // (2) Setup the factorization machine
        fm_model fm = new fm_model();
        
        fm.num_attribute = num_all_attribute;
        fm.init_stdev = initStdev;
        // set the number of dimensions in the factorization
        
        fm.k0 = dim[0] != 0;
        fm.k1 = dim[1] != 0;
        fm.num_factor = dim[2];					
        	
        fm.init();


        // (3) Setup the learning method:
        fm_learn fml;
        
        if (method==FactorizationMethod.SGD) {
                fml = new fm_learn_sgd_element();
                ((fm_learn_sgd)fml).num_iter = (numIter>0)?numIter:100; //Fallback to default

        } else if (method==FactorizationMethod.SGDA) {
                fml = new fm_learn_sgd_element_adapt_reg();
                ((fm_learn_sgd)fml).num_iter = (numIter>0)?numIter:100;
                ((fm_learn_sgd_element_adapt_reg)fml).validation = validation;

        } else if (method==FactorizationMethod.MCMC) {
                fm.w.init_normal(fm.init_mean, fm.init_stdev);
                fml = new fm_learn_mcmc_simultaneous();
                ((fm_learn_mcmc)fml).num_iter = (numIter>0)?numIter:100;
                ((fm_learn_mcmc)fml).num_eval_cases = (numEvalCases>0)?numIter:test.num_cases;

                ((fm_learn_mcmc)fml).do_sample = doSampling;
                ((fm_learn_mcmc)fml).do_multilevel = doMultilevel;
        } else {
                throw new IllegalArgumentException("unknown method");
        }
        fml.fm = fm;
        fml.max_target = train.max_target;
        fml.min_target = train.min_target;
        fml.meta = meta;
        if (task==TaskType.Regression ) {
                fml.task = TaskType.Regression;
        } else if (task==TaskType.Classification  ) {
                fml.task = TaskType.Classification;
                for (int i = 0; i < train.target.dim; i++) { if (train.target.get(i) <= 0.0) { train.target.set(i,-1.0f); } else {train.target.set(i,1.0f); } }
                for (int i = 0; i < test.target.dim; i++) { if (test.target.get(i) <= 0.0) { test.target.set(i,-1.0f); } else {test.target.set(i,1.0f); } }
        } else {
                throw new IllegalArgumentException("unknown task");
        }


        fml.log = null;
        fml.init();
        if (method == FactorizationMethod.MCMC) {
                // set the regularization; for als and mcmc this can be individual per group
                { 
                        ///just different naming for convenience
                        double[] reg = regular;
                        if(reg!=null&&reg.length!=0&&reg.length!=1&&reg.length!=3&&reg.length!=1+meta.num_attr_groups*2){
                            throw new IllegalArgumentException("Parameter Regular's length has to be one of the following values: 0,1,3,"+1+meta.num_attr_groups*2);
                        }
                        if (reg == null || reg.length == 0) {
                                fm.reg0 = 0.0;
                                fm.regw = 0.0;
                                fm.regv = 0.0;
                                ((fm_learn_mcmc)fml).w_lambda.init(fm.regw);
                                ((fm_learn_mcmc)fml).v_lambda.init(fm.regv);
                        } else if (reg.length == 1) {
                                fm.reg0 = reg[0];
                                fm.regw = reg[0];
                                fm.regv = reg[0];
                                ((fm_learn_mcmc)fml).w_lambda.init(fm.regw);
                                ((fm_learn_mcmc)fml).v_lambda.init(fm.regv);					
                        } else if (reg.length == 3) {
                                fm.reg0 = reg[0];
                                fm.regw = reg[1];
                                fm.regv = reg[2];
                                ((fm_learn_mcmc)fml).w_lambda.init(fm.regw);
                                ((fm_learn_mcmc)fml).v_lambda.init(fm.regv);
                        } else {
                        		///Reg should be 1+meta.num_attr_groups*2 in this case
                                fm.reg0 = reg[0];
                                fm.regw = 0.0;
                                fm.regv = 0.0;
                                int j = 1;
                                for (int g = 0; g < meta.num_attr_groups; g++) {
                                        ((fm_learn_mcmc)fml).w_lambda.set(g ,reg[j]);
                                        j++;
                                }
                                for (int g = 0; g < meta.num_attr_groups; g++) {
                                        for (int f = 0; f < fm.num_factor; f++) {
                                                ((fm_learn_mcmc)fml).v_lambda.set(g,f,reg[j]);
                                        }
                                        j++;
                                }
                        }

                }
        } else {
                // set the regularization; for standard SGD, groups are not supported
                { 
                        ///just different naming for convenience
                        double[] reg = regular;
                        if(reg.length!=0&&reg.length!=1&&reg.length!=3){
                            throw new IllegalArgumentException("Parameter Regular's length has to be one of the following values: 0,1,3");
                        }
                        if (reg.length == 0) {
                                fm.reg0 = 0.0;
                                fm.regw = 0.0;
                                fm.regv = 0.0;
                        } else if (reg.length == 1) {
                                fm.reg0 = reg[0];
                                fm.regw = reg[0];
                                fm.regv = reg[0];
                        } else {
                                fm.reg0 = reg[0];
                                fm.regw = reg[1];
                                fm.regv = reg[2];
                        }		
                }
        }
        
        ///in original: dynamic cast to test if object has superclass
        if (fm_learn_sgd.class.isAssignableFrom(fml.getClass())) {
                // set the learning rates (individual per layer)

                fm_learn_sgd fmlsgd= (fm_learn_sgd)fml; 
                double[] lr = learnRates;
                if(lr.length!=1&&lr.length!=3){
                            throw new IllegalArgumentException("Parameter Learn Rates' length has to be one of the following values: 1,3");
                        }
                if (lr.length == 1) {
                        fmlsgd.learn_rate = lr[0];
                        fmlsgd.learn_rates.init(lr[0]);
                } else {
                        fmlsgd.learn_rate = 0;
                        fmlsgd.learn_rates.set(0, lr[0]);
                        fmlsgd.learn_rates.set(1, lr[1]);
                        fmlsgd.learn_rates.set(2, lr[2]);
                }		

        }

        if (debugVerbosity) { 
                fm.debug();			
                fml.debug();			
        }	
        
        _fml = fml;
        _train = train;
        _test = test;
        _method = method;
    }
    
    /**
     * This method is calls the learn method of the specific fml_learn subclass. This subclass implements different behavior depending on which algorithm was chosen. 
     * When the class LibFmRecommender calls this method, the _test object is empty. Operations are then only done on the _train object.
     */
    public void Learn(){
        // () learn		
        _fml.learn(_train, _test);
    }
    
    /**
     * This method predicts the rating for a users-item-tuple. It is used by the LibFMRecommender to predict ratings for R101.
     * @param user
     * @param item
     * @return The prediction for the rating
     */
	public float PredictRating(int user, int item) {
        ///A data model is created for each tuple. This may be overkill, but this way the least code inside of libfm had to be changed.
		///This might be replaced by a more lightweight way later on.
        DataModel singleModel = new DataModel();
        Rating r = new Rating(user, item, 1);
        singleModel.addRating(r);
        if(_contextEnabled&&_globalExtraInfo!=null){
        	///To date the only method to obtain extraInformation for the test data is via a global data structure. 
        	///This data structure was loaded beforehand via reflection from the R101 parameter
        	///A null pointer exception can happen here, which is OK, because if there is no context for the tuple the algorithm will fail anyways
        	singleModel.addExtraInformation(r, _globalExtraInfo.get(r.user).get(r.item));
        }
        
        try {
        	///A Data object is created for the tuple 
			_test.loadFromR101DataModel(singleModel, _idMap, 0, _contextEnabled);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        ///The actual prediction happens here
        return PredictSingleRating();
	}
	
	/**
	 * This method handles the actual prediction of a rating for one single
	 * user-item-tuple after the _test object has been filled with the tuple.
	 * 
	 * @return The prediction for the rating
	 */
	private float PredictSingleRating() {
	    ///The data structure for the retrieval is created
		
	    ///Can these values be obtained from R101 somehow?
	    _test.max_target = _maxRating;
	    _test.min_target = _minRating;
	    
	    if(_method==FactorizationMethod.MCMC||_method==FactorizationMethod.ALS){
	    	///If our algorithm is MCMC or ALS we can't just call _fml.predict
	    	///This is because MCMC and ALS normally predict the ratings parallel to the training process. The predict method merely retrieves them, which cant't be done, because in R101 the test data is not present while the training happens.
	    	///Instead we have to call the method predict_data_and_write_to_eterms. The following code creates the necessary data structures and calls the method.
	    	fm_learn_mcmc mcmcMachine = ((fm_learn_mcmc)_fml);
	    	_test.data.begin();
	    	///A workaround is following. This is very different from the original but necessary for speed, because it avoids transposition.
	    	return mcmcMachine.predict_data_and_write_to_eterms(_test.data.getRow());
	    }
	    else{
	    	DVectorDouble pred = new DVectorDouble();
		    pred.setSize(1);
		    pred.init(0.0);
	    	///If the algorithm is SGD or SGDA, just call _fml.predict
	    	_fml.predict(_test, pred);
			return (float) (double )pred.get(0);
	    }
	    	
	    
	}
}
