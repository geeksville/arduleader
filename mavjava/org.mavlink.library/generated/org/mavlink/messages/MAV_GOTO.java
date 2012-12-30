/**
 * Generated class : MAV_GOTO
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface MAV_GOTO
 * Override command, pauses current mission execution and moves immediately to a position
 **/
public interface MAV_GOTO {
    /**
     * Hold at the current position.
     */
    public final static int MAV_GOTO_DO_HOLD = 0;
    /**
     * Continue with the next item in mission execution.
     */
    public final static int MAV_GOTO_DO_CONTINUE = 1;
    /**
     * Hold at the current position of the system
     */
    public final static int MAV_GOTO_HOLD_AT_CURRENT_POSITION = 2;
    /**
     * Hold at the position specified in the parameters of the DO_HOLD action
     */
    public final static int MAV_GOTO_HOLD_AT_SPECIFIED_POSITION = 3;
}
