/**
 * Generated class : MAV_MOUNT_MODE
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface MAV_MOUNT_MODE
 * Enumeration of possible mount operation modes
 **/
public interface MAV_MOUNT_MODE {
    /**
     * Load and keep safe position (Roll,Pitch,Yaw) from EEPROM and stop stabilization
     */
    public final static int MAV_MOUNT_MODE_RETRACT = 0;
    /**
     * Load and keep neutral position (Roll,Pitch,Yaw) from EEPROM.
     */
    public final static int MAV_MOUNT_MODE_NEUTRAL = 1;
    /**
     * Load neutral position and start MAVLink Roll,Pitch,Yaw control with stabilization
     */
    public final static int MAV_MOUNT_MODE_MAVLINK_TARGETING = 2;
    /**
     * Load neutral position and start RC Roll,Pitch,Yaw control with stabilization
     */
    public final static int MAV_MOUNT_MODE_RC_TARGETING = 3;
    /**
     * Load neutral position and start to point to Lat,Lon,Alt
     */
    public final static int MAV_MOUNT_MODE_GPS_POINT = 4;
}
