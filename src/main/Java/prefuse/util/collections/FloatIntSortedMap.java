package prefuse.util.collections;


/**
 * Sorted map that maps from a float key to an int value.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface FloatIntSortedMap extends IntSortedMap {

    float firstKey();

    float lastKey();

    boolean containsKey(float key);
    
    IntIterator valueRangeIterator(float fromKey, boolean fromInc,
                                   float toKey, boolean toInc);
    
    LiteralIterator keyIterator();

    LiteralIterator keyRangeIterator(float fromKey, boolean fromInc,
                                     float toKey, boolean toInc);

    int get(float key);

    int remove(float key);
    
    int remove(float key, int value);

    int put(float key, int value);
    
} // end of interface FloatIntSortedMap
