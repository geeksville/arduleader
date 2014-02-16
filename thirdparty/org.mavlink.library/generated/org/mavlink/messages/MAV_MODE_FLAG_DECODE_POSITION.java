/**
 * Generated class : MAV_MODE_FLAG_DECODE_POSITION
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface MAV_MODE_FLAG_DECODE_POSITION
 * These values encode the bit positions of the decode position. These values can be used to read the value of a flag bit by combining the base_mode variable with AND with the flag position value. The result will be either 0 or 1, depending on if the flag is set or not.
 **/
public interface MAV_MODE_FLAG_DECODE_POSITION {
    /**
     * First bit:  10000000
     */
    public final static int MAV_MODE_FLAG_DECODE_POSITION_SAFETY = 128;
    /**
     * Second bit: 01000000
     */
    public final static int MAV_MODE_FLAG_DECODE_POSITION_MANUAL = 64;
    /**
     * Third bit:  00100000
     */
    public final static int MAV_MODE_FLAG_DECODE_POSITION_HIL = 32;
    /**
     * Fourth bit: 00010000
     */
    public final static int MAV_MODE_FLAG_DECODE_POSITION_STABILIZE = 16;
    /**
     * Fifth bit:  00001000
     */
    public final static int MAV_MODE_FLAG_DECODE_POSITION_GUIDED = 8;
    /**
     * Sixt bit:   00000100
     */
    public final static int MAV_MODE_FLAG_DECODE_POSITION_AUTO = 4;
    /**
     * Seventh bit: 00000010
     */
    public final static int MAV_MODE_FLAG_DECODE_POSITION_TEST = 2;
    /**
     * Eighth bit: 00000001
     */
    public final static int MAV_MODE_FLAG_DECODE_POSITION_CUSTOM_MODE = 1;
}
