package org.recommender101.recommender.baseline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.recommender101.data.Rating;
import org.recommender101.recommender.AbstractRecommender;

/**
 * Recommends random items
 * @author MJ
 *
 */
@SuppressWarnings("serial")
public class Random extends AbstractRecommender{
	
	private boolean hideKnownItems = true;

	@Override
	public float predictRating(int user, int item) {
		return Float.NaN;
	}

	@Override
	public List<Integer> recommendItems(int user) {
		Set<Integer> itemSet = new HashSet<>(getDataModel().getItems());
		
		//if we don't want to recommend items already known to the user, we have to filter these here
		if(hideKnownItems){
			Set<Rating> ratingsOfUser = getDataModel().getRatingsOfUser(user);
			
			for (Rating rating : ratingsOfUser) {
				itemSet.remove(rating.item);
			}
		}
		
		//recommend the whole item set and shuffle it beforehand to make the recommendations random
		List<Integer> itemList = new ArrayList<Integer>(itemSet);
		Collections.shuffle(itemList);
		
		return itemList;
	}

	@Override
	public void init() throws Exception {}
	
	@Override
	public void hideKnownItems(boolean value) {
		this.hideKnownItems = value;
	}

}
