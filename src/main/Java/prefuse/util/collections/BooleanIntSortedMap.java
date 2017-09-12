package prefuse.util.collections;

/**
 * Sorted map that maps from a boolean key to an int value.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface BooleanIntSortedMap extends IntSortedMap {

    boolean firstKey();

    boolean lastKey();

    boolean containsKey(boolean key);
    
    IntIterator valueRangeIterator(boolean fromKey, boolean fromInc,
                                   boolean toKey, boolean toInc);
    
    LiteralIterator keyIterator();

    LiteralIterator keyRangeIterator(boolean fromKey, boolean fromInc,
                                     boolean toKey, boolean toInc);

    int get(boolean key);

    int remove(boolean key);
    
    int remove(boolean key, int value);

    int put(boolean key, int value);
    
} // end of interface LongIntSortedMap
