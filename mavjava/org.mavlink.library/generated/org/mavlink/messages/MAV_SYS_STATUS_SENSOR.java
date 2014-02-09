/**
 * Generated class : MAV_SYS_STATUS_SENSOR
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
/**
 * Interface MAV_SYS_STATUS_SENSOR
 * These encode the sensors whose status is sent as part of the SYS_STATUS message.
 **/
public interface MAV_SYS_STATUS_SENSOR {
    /**
     * 0x01 3D gyro
     */
    public final static int MAV_SYS_STATUS_SENSOR_3D_GYRO = 1;
    /**
     * 0x02 3D accelerometer
     */
    public final static int MAV_SYS_STATUS_SENSOR_3D_ACCEL = 2;
    /**
     * 0x04 3D magnetometer
     */
    public final static int MAV_SYS_STATUS_SENSOR_3D_MAG = 4;
    /**
     * 0x08 absolute pressure
     */
    public final static int MAV_SYS_STATUS_SENSOR_ABSOLUTE_PRESSURE = 8;
    /**
     * 0x10 differential pressure
     */
    public final static int MAV_SYS_STATUS_SENSOR_DIFFERENTIAL_PRESSURE = 16;
    /**
     * 0x20 GPS
     */
    public final static int MAV_SYS_STATUS_SENSOR_GPS = 32;
    /**
     * 0x40 optical flow
     */
    public final static int MAV_SYS_STATUS_SENSOR_OPTICAL_FLOW = 64;
    /**
     * 0x80 computer vision position
     */
    public final static int MAV_SYS_STATUS_SENSOR_VISION_POSITION = 128;
    /**
     * 0x100 laser based position
     */
    public final static int MAV_SYS_STATUS_SENSOR_LASER_POSITION = 256;
    /**
     * 0x200 external ground truth (Vicon or Leica)
     */
    public final static int MAV_SYS_STATUS_SENSOR_EXTERNAL_GROUND_TRUTH = 512;
    /**
     * 0x400 3D angular rate control
     */
    public final static int MAV_SYS_STATUS_SENSOR_ANGULAR_RATE_CONTROL = 1024;
    /**
     * 0x800 attitude stabilization
     */
    public final static int MAV_SYS_STATUS_SENSOR_ATTITUDE_STABILIZATION = 2048;
    /**
     * 0x1000 yaw position
     */
    public final static int MAV_SYS_STATUS_SENSOR_YAW_POSITION = 4096;
    /**
     * 0x2000 z/altitude control
     */
    public final static int MAV_SYS_STATUS_SENSOR_Z_ALTITUDE_CONTROL = 8192;
    /**
     * 0x4000 x/y position control
     */
    public final static int MAV_SYS_STATUS_SENSOR_XY_POSITION_CONTROL = 16384;
    /**
     * 0x8000 motor outputs / control
     */
    public final static int MAV_SYS_STATUS_SENSOR_MOTOR_OUTPUTS = 32768;
    /**
     * 0x10000 rc receiver
     */
    public final static int MAV_SYS_STATUS_SENSOR_RC_RECEIVER = 65536;
}
