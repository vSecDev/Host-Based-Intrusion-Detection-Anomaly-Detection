package prefuse.util.collections;

import java.util.Iterator;

/**
 * Sorted map that maps from an Object key to an int value.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface ObjectIntSortedMap extends IntSortedMap {

    Object MAX_KEY = new Object();
    Object MIN_KEY = new Object();
    
    Object firstKey();

    Object lastKey();

    boolean containsKey(Object key);
    
    IntIterator valueRangeIterator(Object fromKey, boolean fromInc,
                                   Object toKey, boolean toInc);
    
    Iterator keyIterator();

    Iterator keyRangeIterator(Object fromKey, boolean fromInc,
                              Object toKey, boolean toInc);

    int get(Object key);

    int remove(Object key);
    
    int remove(Object key, int val);

    int put(Object key, int value);
    
} // end of interface ObjectIntSortedMap
