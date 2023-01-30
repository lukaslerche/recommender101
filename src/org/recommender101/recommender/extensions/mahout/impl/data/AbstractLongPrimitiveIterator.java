package org.recommender101.recommender.extensions.mahout.impl.data;

/**
 * Iterator for Longs
 * Adapted from previous Apache Mahout implementation (0.4)
 *
 */
public abstract class AbstractLongPrimitiveIterator implements LongPrimitiveIterator {
  
  @Override
  public Long next() {
    return nextLong();
  }
  
}
