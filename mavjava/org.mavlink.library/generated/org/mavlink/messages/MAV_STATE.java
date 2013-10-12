/**
 * Generated class : MAV_STATE
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface MAV_STATE
 * 
 **/
public interface MAV_STATE {
    /**
     * Uninitialized system, state is unknown.
     */
    public final static int MAV_STATE_UNINIT = 0;
    /**
     * System is booting up.
     */
    public final static int MAV_STATE_BOOT = 1;
    /**
     * System is calibrating and not flight-ready.
     */
    public final static int MAV_STATE_CALIBRATING = 2;
    /**
     * System is grounded and on standby. It can be launched any time.
     */
    public final static int MAV_STATE_STANDBY = 3;
    /**
     * System is active and might be already airborne. Motors are engaged.
     */
    public final static int MAV_STATE_ACTIVE = 4;
    /**
     * System is in a non-normal flight mode. It can however still navigate.
     */
    public final static int MAV_STATE_CRITICAL = 5;
    /**
     * System is in a non-normal flight mode. It lost control over parts or over the whole airframe. It is in mayday and going down.
     */
    public final static int MAV_STATE_EMERGENCY = 6;
    /**
     * System just initialized its power-down sequence, will shut down now.
     */
    public final static int MAV_STATE_POWEROFF = 7;
}
