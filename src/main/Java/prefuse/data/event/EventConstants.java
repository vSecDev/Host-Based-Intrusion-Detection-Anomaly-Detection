package prefuse.data.event;

import javax.swing.event.TableModelEvent;

/**
 * Constants used within prefuse data structure modification notifications.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface EventConstants {

    /** Indicates a data insert operation. */
    int INSERT = TableModelEvent.INSERT;
    /** Indicates a data update operation. */
    int UPDATE = TableModelEvent.UPDATE;
    /** Indicates a data delete operation. */
    int DELETE = TableModelEvent.DELETE;
    /** Indicates an operation that affects all columns of a table. */
    int ALL_COLUMNS = TableModelEvent.ALL_COLUMNS;
    
} // end of interface EventConstants
