/**
 * Generated class : MAV_ROI
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface MAV_ROI
 * The ROI (region of interest) for the vehicle. This can be
                be used by the vehicle for camera/vehicle attitude alignment (see
                MAV_CMD_NAV_ROI).
 **/
public interface MAV_ROI {
    /**
     * No region of interest.
     */
    public final static int MAV_ROI_NONE = 0;
    /**
     * Point toward next MISSION.
     */
    public final static int MAV_ROI_WPNEXT = 1;
    /**
     * Point toward given MISSION.
     */
    public final static int MAV_ROI_WPINDEX = 2;
    /**
     * Point toward fixed location.
     */
    public final static int MAV_ROI_LOCATION = 3;
    /**
     * Point toward of given id.
     */
    public final static int MAV_ROI_TARGET = 4;
}
