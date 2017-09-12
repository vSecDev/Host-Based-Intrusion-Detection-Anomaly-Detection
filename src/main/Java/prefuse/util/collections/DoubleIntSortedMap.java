package prefuse.util.collections;


/**
 * Sorted map that maps from a double key to an int value.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface DoubleIntSortedMap extends IntSortedMap {

    double firstKey();

    double lastKey();

    boolean containsKey(double key);
    
    IntIterator valueRangeIterator(double fromKey, boolean fromInc,
                                   double toKey, boolean toInc);
    
    LiteralIterator keyIterator();
    
    LiteralIterator keyRangeIterator(double fromKey, boolean fromInc,
                                     double toKey, boolean toInc);

    int get(double key);

    int remove(double key);
    
    int remove(double key, int value);

    int put(double key, int value);
    
} // end of interface DoubleIntSortedMap
