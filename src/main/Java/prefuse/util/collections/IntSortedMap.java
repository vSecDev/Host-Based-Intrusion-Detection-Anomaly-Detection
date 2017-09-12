package prefuse.util.collections;

import java.util.Comparator;


/**
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface IntSortedMap {
  
    int getMinimum();
    int getMaximum();
    int getMedian();
    int getUniqueCount();
    
    boolean isAllowDuplicates();
    int size();
    boolean isEmpty();
    Comparator comparator();
    
    void clear();
    boolean containsValue(int value);
    IntIterator valueIterator(boolean ascending);
    
}
