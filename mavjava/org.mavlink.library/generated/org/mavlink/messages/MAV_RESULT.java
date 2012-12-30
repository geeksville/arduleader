/**
 * Generated class : MAV_RESULT
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface MAV_RESULT
 * result from a mavlink command
 **/
public interface MAV_RESULT {
    /**
     * Command ACCEPTED and EXECUTED
     */
    public final static int MAV_RESULT_ACCEPTED = 0;
    /**
     * Command TEMPORARY REJECTED/DENIED
     */
    public final static int MAV_RESULT_TEMPORARILY_REJECTED = 1;
    /**
     * Command PERMANENTLY DENIED
     */
    public final static int MAV_RESULT_DENIED = 2;
    /**
     * Command UNKNOWN/UNSUPPORTED
     */
    public final static int MAV_RESULT_UNSUPPORTED = 3;
    /**
     * Command executed, but failed
     */
    public final static int MAV_RESULT_FAILED = 4;
}
