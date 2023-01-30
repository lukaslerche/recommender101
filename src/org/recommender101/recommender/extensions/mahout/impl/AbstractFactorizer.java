/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.recommender101.recommender.extensions.mahout.impl;

import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.recommender101.data.DataModel;
import org.recommender101.data.Rating;
import org.recommender101.recommender.extensions.mahout.impl.data.FastByIDMap;
import org.recommender101.recommender.extensions.mahout.impl.exception.TasteException;

/**
 * Original code by Apache Mahout Project.
 * Edited for Recommender101.
 * base class for {@link Factorizer}s, provides ID to index mapping
 */
public abstract class AbstractFactorizer implements Factorizer {

	protected static final Random random = new Random();
  private final DataModel dataModel;
  private FastByIDMap<Integer> userIDMapping;
  private FastByIDMap<Integer> itemIDMapping;

  protected AbstractFactorizer(DataModel dataModel) throws TasteException {
    this.dataModel = dataModel;
    buildMappings();
  }
  
  private void buildMappings() throws TasteException {
    userIDMapping = createIDMapping(dataModel.getUsers());
    itemIDMapping = createIDMapping(dataModel.getItems());
  }

  protected Factorization createFactorization(double[][] userFeatures, double[][] itemFeatures) {
    return new Factorization(userIDMapping, itemIDMapping, userFeatures, itemFeatures);
  }

  protected Integer userIndex(long userID) {
    Integer userIndex = userIDMapping.get(userID);
    if (userIndex == null) {
      userIndex = userIDMapping.size();
      userIDMapping.put(userID, userIndex);
    }
    return userIndex;
  }

  protected Integer itemIndex(long itemID) {
    Integer itemIndex = itemIDMapping.get(itemID);
    if (itemIndex == null) {
      itemIndex = itemIDMapping.size();
      itemIDMapping.put(itemID, itemIndex);
    }
    return itemIndex;
  }

  private static FastByIDMap<Integer> createIDMapping(Set<Integer> set) {
    FastByIDMap<Integer> mapping = new FastByIDMap<Integer>(set.size());
    int index = 0;
    for (Integer id : set) {
    	mapping.put(id, index++);
	}
    return mapping;
  }
  
  protected double getAveragePreference() throws TasteException {
	    SummaryStatistics stat = new SummaryStatistics();
	    
	    for (Rating rating : dataModel.getRatings()) {
			stat.addValue(rating.rating);
		}
	    
	    return stat.getMean();
	  }
}
