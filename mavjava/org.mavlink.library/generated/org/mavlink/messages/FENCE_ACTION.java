/**
 * Generated class : FENCE_ACTION
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface FENCE_ACTION
 * 
 **/
public interface FENCE_ACTION {
    /**
     * Disable fenced mode
     */
    public final static int FENCE_ACTION_NONE = 0;
    /**
     * Switched to guided mode to return point (fence point 0)
     */
    public final static int FENCE_ACTION_GUIDED = 1;
    /**
     * Report fence breach, but don't take action
     */
    public final static int FENCE_ACTION_REPORT = 2;
    /**
     * Switched to guided mode to return point (fence point 0) with manual throttle control
     */
    public final static int FENCE_ACTION_GUIDED_THR_PASS = 3;
}
