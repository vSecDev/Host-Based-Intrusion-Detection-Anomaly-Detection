package prefuse.util.collections;

/**
 * Sorted map that maps from an int key to an int value.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface IntIntSortedMap extends IntSortedMap {

    int firstKey();

    int lastKey();

    boolean containsKey(int key);
    
    IntIterator valueRangeIterator(int fromKey, boolean fromInc,
                                   int toKey, boolean toInc);
    
    LiteralIterator keyIterator();
    
    LiteralIterator keyRangeIterator(int fromKey, boolean fromInc,
                                     int toKey, boolean toInc);

    int get(int key);

    int remove(int key);
    
    int remove(int key, int value);

    int put(int key, int value);
    
    int getLast(int key);
    
    int getNextValue(int key, int value);
    
    int getPreviousValue(int key, int value);
    
} // end of interface IntIntSortedMap
