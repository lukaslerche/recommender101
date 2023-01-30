package org.recommender101.recommender.extensions.mahout.impl.data;

import java.util.Iterator;
import java.util.Set;
/**
 * A helper class to iterate a standard java set of integers inside the mahout framework.
 * 
 * @author MJ
 *
 */
public class LongPrimitiveSetIterator implements LongPrimitiveIterator{
	private Iterator<Integer> iterator;

	public LongPrimitiveSetIterator(Set<Integer> set){
		iterator = set.iterator();
	  }
	  
	@Override
	public void skip(int n) { throw new UnsupportedOperationException();	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Long next() {
		return (long) iterator.next();
	}

	@Override
	public long nextLong() {
		return (long) iterator.next();
	}

	@Override
	public long peek() { throw new UnsupportedOperationException();	}

	@Override
	public void remove() {
		iterator.remove();
	}
	  
  }