/**
 * Generated class : msg_sys_status
 * DO NOT MODIFY!
 **/
package org.mavlink.messages.ardupilotmega;
import org.mavlink.messages.MAVLinkMessage;
import org.mavlink.IMAVLinkCRC;
import org.mavlink.MAVLinkCRC;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
/**
 * Class msg_sys_status
 * The general system state. If the system is following the MAVLink standard, the system state is mainly defined by three orthogonal states/modes: The system mode, which is either LOCKED (motors shut down and locked), MANUAL (system under RC control), GUIDED (system with autonomous position control, position setpoint controlled manually) or AUTO (system guided by path/waypoint planner). The NAV_MODE defined the current flight state: LIFTOFF (often an open-loop maneuver), LANDING, WAYPOINTS or VECTOR. This represents the internal navigation state machine. The system status shows wether the system is currently active or not and if an emergency occured. During the CRITICAL and EMERGENCY states the MAV is still considered to be active, but should start emergency procedures autonomously. After a failure occured it should first move from active to critical to allow manual intervention and then move to emergency after a certain timeout.
 **/
public class msg_sys_status extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SYS_STATUS = 1;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SYS_STATUS;
  public msg_sys_status(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SYS_STATUS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 31;
}

  /**
   * Bitmask showing which onboard controllers and sensors are present. Value of 0: not present. Value of 1: present. Indices defined by ENUM MAV_SYS_STATUS_SENSOR
   */
  public long onboard_control_sensors_present;
  /**
   * Bitmask showing which onboard controllers and sensors are enabled:  Value of 0: not enabled. Value of 1: enabled. Indices defined by ENUM MAV_SYS_STATUS_SENSOR
   */
  public long onboard_control_sensors_enabled;
  /**
   * Bitmask showing which onboard controllers and sensors are operational or have an error:  Value of 0: not enabled. Value of 1: enabled. Indices defined by ENUM MAV_SYS_STATUS_SENSOR
   */
  public long onboard_control_sensors_health;
  /**
   * Maximum usage in percent of the mainloop time, (0%: 0, 100%: 1000) should be always below 1000
   */
  public int load;
  /**
   * Battery voltage, in millivolts (1 = 1 millivolt)
   */
  public int voltage_battery;
  /**
   * Battery current, in 10*milliamperes (1 = 10 milliampere), -1: autopilot does not measure the current
   */
  public int current_battery;
  /**
   * Communication drops in percent, (0%: 0, 100%: 10'000), (UART, I2C, SPI, CAN), dropped packets on all links (packets that were corrupted on reception on the MAV)
   */
  public int drop_rate_comm;
  /**
   * Communication errors (UART, I2C, SPI, CAN), dropped packets on all links (packets that were corrupted on reception on the MAV)
   */
  public int errors_comm;
  /**
   * Autopilot-specific errors
   */
  public int errors_count1;
  /**
   * Autopilot-specific errors
   */
  public int errors_count2;
  /**
   * Autopilot-specific errors
   */
  public int errors_count3;
  /**
   * Autopilot-specific errors
   */
  public int errors_count4;
  /**
   * Remaining battery energy: (0%: 0, 100%: 100), -1: autopilot estimate the remaining battery
   */
  public int battery_remaining;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  onboard_control_sensors_present = (int)dis.getInt()&0x00FFFFFFFF;
  onboard_control_sensors_enabled = (int)dis.getInt()&0x00FFFFFFFF;
  onboard_control_sensors_health = (int)dis.getInt()&0x00FFFFFFFF;
  load = (int)dis.getShort()&0x00FFFF;
  voltage_battery = (int)dis.getShort()&0x00FFFF;
  current_battery = (int)dis.getShort();
  drop_rate_comm = (int)dis.getShort()&0x00FFFF;
  errors_comm = (int)dis.getShort()&0x00FFFF;
  errors_count1 = (int)dis.getShort()&0x00FFFF;
  errors_count2 = (int)dis.getShort()&0x00FFFF;
  errors_count3 = (int)dis.getShort()&0x00FFFF;
  errors_count4 = (int)dis.getShort()&0x00FFFF;
  battery_remaining = (int)dis.get();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+31];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(onboard_control_sensors_present&0x00FFFFFFFF));
  dos.putInt((int)(onboard_control_sensors_enabled&0x00FFFFFFFF));
  dos.putInt((int)(onboard_control_sensors_health&0x00FFFFFFFF));
  dos.putShort((short)(load&0x00FFFF));
  dos.putShort((short)(voltage_battery&0x00FFFF));
  dos.putShort((short)(current_battery&0x00FFFF));
  dos.putShort((short)(drop_rate_comm&0x00FFFF));
  dos.putShort((short)(errors_comm&0x00FFFF));
  dos.putShort((short)(errors_count1&0x00FFFF));
  dos.putShort((short)(errors_count2&0x00FFFF));
  dos.putShort((short)(errors_count3&0x00FFFF));
  dos.putShort((short)(errors_count4&0x00FFFF));
  dos.put((byte)(battery_remaining&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 31);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[37] = crcl;
  buffer[38] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SYS_STATUS : " +   "  onboard_control_sensors_present="+onboard_control_sensors_present+  "  onboard_control_sensors_enabled="+onboard_control_sensors_enabled+  "  onboard_control_sensors_health="+onboard_control_sensors_health+  "  load="+load+  "  voltage_battery="+voltage_battery+  "  current_battery="+current_battery+  "  drop_rate_comm="+drop_rate_comm+  "  errors_comm="+errors_comm+  "  errors_count1="+errors_count1+  "  errors_count2="+errors_count2+  "  errors_count3="+errors_count3+  "  errors_count4="+errors_count4+  "  battery_remaining="+battery_remaining;}
}
