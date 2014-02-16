/**
 * Generated class : LIMITS_STATE
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface LIMITS_STATE
 * 
 **/
public interface LIMITS_STATE {
    /**
     * pre-initialization
     */
    public final static int LIMITS_INIT = 0;
    /**
     * disabled
     */
    public final static int LIMITS_DISABLED = 1;
    /**
     * checking limits
     */
    public final static int LIMITS_ENABLED = 2;
    /**
     * a limit has been breached
     */
    public final static int LIMITS_TRIGGERED = 3;
    /**
     * taking action eg. RTL
     */
    public final static int LIMITS_RECOVERING = 4;
    /**
     * we're no longer in breach of a limit
     */
    public final static int LIMITS_RECOVERED = 5;
}
