package prefuse.controls;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EventListener;

import prefuse.visual.VisualItem;


/**
 * Listener interface for processing user interface events on a Display.
 * 
 * @author alan newberger
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface Control extends EventListener, 
    MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
    /** Represents the use of the left mouse button */
    int LEFT_MOUSE_BUTTON   = MouseEvent.BUTTON1_MASK;
    /** Represents the use of the middle mouse button */
    int MIDDLE_MOUSE_BUTTON = MouseEvent.BUTTON2_MASK;
    /** Represents the use of the right mouse button */
    int RIGHT_MOUSE_BUTTON  = MouseEvent.BUTTON3_MASK;
    
    /**
     * Indicates if this Control is currently enabled.
     * @return true if the control is enabled, false if disabled
     */
    boolean isEnabled();
    
    /**
     * Sets the enabled status of this control.
     * @param enabled true to enable the control, false to disable it
     */
    void setEnabled(boolean enabled);
    
    // -- Actions performed on VisualItems ------------------------------------

    /**
     * Invoked when a mouse button is pressed on a VisualItem and then dragged.
     */
    void itemDragged(VisualItem item, MouseEvent e);
    
    /**
     * Invoked when the mouse cursor has been moved onto a VisualItem but
     *  no buttons have been pushed.
     */
    void itemMoved(VisualItem item, MouseEvent e);
    
    /**
     * Invoked when the mouse wheel is rotated while the mouse is over a
     *  VisualItem.
     */
    void itemWheelMoved(VisualItem item, MouseWheelEvent e);
    
    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     *  a VisualItem.
     */
    void itemClicked(VisualItem item, MouseEvent e);
    
    /**
     * Invoked when a mouse button has been pressed on a VisualItem.
     */
    void itemPressed(VisualItem item, MouseEvent e);
    
    /**
     * Invoked when a mouse button has been released on a VisualItem.
     */
    void itemReleased(VisualItem item, MouseEvent e);
    
    /**
     * Invoked when the mouse enters a VisualItem.
     */
    void itemEntered(VisualItem item, MouseEvent e);
    
    /**
     * Invoked when the mouse exits a VisualItem.
     */
    void itemExited(VisualItem item, MouseEvent e);
    
    /**
     * Invoked when a key has been pressed, while the mouse is over
     *  a VisualItem.
     */
    void itemKeyPressed(VisualItem item, KeyEvent e);
    
    /**
     * Invoked when a key has been released, while the mouse is over
     *  a VisualItem.
     */
    void itemKeyReleased(VisualItem item, KeyEvent e);
    
    /**
     * Invoked when a key has been typed, while the mouse is over
     *  a VisualItem.
     */
    void itemKeyTyped(VisualItem item, KeyEvent e);
    
    
    // -- Actions performed on the Display ------------------------------------
    
    /**
     * Invoked when the mouse enters the Display.
     */
    void mouseEntered(MouseEvent e);
    
    /**
     * Invoked when the mouse exits the Display.
     */
    void mouseExited(MouseEvent e);
    
    /**
     * Invoked when a mouse button has been pressed on the Display but NOT
     *  on a VisualItem.
     */
    void mousePressed(MouseEvent e);
    
    /**
     * Invoked when a mouse button has been released on the Display but NOT
     *  on a VisualItem.
     */
    void mouseReleased(MouseEvent e);
    
    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     *  the Display, but NOT on a VisualItem.
     */
    void mouseClicked(MouseEvent e);
    
    /**
     * Invoked when a mouse button is pressed on the Display (but NOT a 
     *  VisualItem) and then dragged.
     */
    void mouseDragged(MouseEvent e);
    
    /**
     * Invoked when the mouse cursor has been moved on the Display (but NOT a
     * VisualItem) and no buttons have been pushed.
     */
    void mouseMoved(MouseEvent e);
    
    /**
     * Invoked when the mouse wheel is rotated while the mouse is over the
     *  Display (but NOT a VisualItem).
     */
    void mouseWheelMoved(MouseWheelEvent e);
    
    /**
     * Invoked when a key has been pressed, while the mouse is NOT 
     *  over a VisualItem.
     */
    void keyPressed(KeyEvent e);
    
    /**
     * Invoked when a key has been released, while the mouse is NOT
     *  over a VisualItem.
     */
    void keyReleased(KeyEvent e);
    
    /**
     * Invoked when a key has been typed, while the mouse is NOT
     *  over a VisualItem.
     */
    void keyTyped(KeyEvent e);

} // end of inteface ControlListener
