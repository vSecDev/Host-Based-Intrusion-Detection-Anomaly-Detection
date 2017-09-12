package prefuse.activity;

import java.util.EventListener;

/**
 * Callback interface by which interested classes can be notified of
 * the progress of a scheduled activity.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface ActivityListener extends EventListener {

    /**
     * Called when an activity has been scheduled with an ActivityManager
     * @param a the scheduled Activity
     */
    void activityScheduled(Activity a);
    
    /**
     * Called when an activity is first started.
     * @param a the started Activity
     */
    void activityStarted(Activity a);
    
    /**
     * Called when an activity is stepped.
     * @param a the stepped Activity
     */
    void activityStepped(Activity a);
    
    /**
     * Called when an activity finishes.
     * @param a the finished Activity
     */
    void activityFinished(Activity a);
    
    /**
     * Called when an activity is cancelled.
     * @param a the cancelled Activity
     */
    void activityCancelled(Activity a);
    
} // end of interface ActivityListener
