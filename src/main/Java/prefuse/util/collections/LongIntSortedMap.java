package prefuse.util.collections;

/**
 * Sorted map that maps from a long key to an int value.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface LongIntSortedMap extends IntSortedMap {

    long firstKey();

    long lastKey();

    boolean containsKey(long key);
    
    IntIterator valueRangeIterator(long fromKey, boolean fromInc,
                                   long toKey, boolean toInc);
    
    LiteralIterator keyIterator();

    LiteralIterator keyRangeIterator(long fromKey, boolean fromInc,
                                     long toKey, boolean toInc);

    int get(long key);

    int remove(long key);
    
    int remove(long key, int value);

    int put(long key, int value);
    
} // end of interface LongIntSortedMap
