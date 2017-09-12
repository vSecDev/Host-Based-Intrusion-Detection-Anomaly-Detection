package prefuse.util.collections;

import java.util.Iterator;

/**
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface LiteralIterator extends Iterator {
    
    int nextInt();
    boolean isIntSupported();
    
    long nextLong();
    boolean isLongSupported();
    
    float nextFloat();
    boolean isFloatSupported();
    
    double nextDouble();
    boolean isDoubleSupported();
    
    boolean nextBoolean();
    boolean isBooleanSupported();
    
} // end of interface LiteralIterator
