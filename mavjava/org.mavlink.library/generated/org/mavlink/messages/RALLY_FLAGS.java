/**
 * Generated class : RALLY_FLAGS
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface RALLY_FLAGS
 * Flags in RALLY_POINT message
 **/
public interface RALLY_FLAGS {
    /**
     * Flag set when requiring favorable winds for landing.
     */
    public final static int FAVORABLE_WIND = 1;
    /**
     * Flag set when plane is to immediately descend to break altitude and land without GCS intervention.  Flag not set when plane is to loiter at Rally point until commanded to land.
     */
    public final static int LAND_IMMEDIATELY = 2;
}
