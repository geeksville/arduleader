/**
 * Generated class : msg_nav_controller_output
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
 * Class msg_nav_controller_output
 * Outputs of the APM navigation controller. The primary use of this message is to check the response and signs of the controller before actual flight and to assist with tuning controller parameters.
 **/
public class msg_nav_controller_output extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT = 62;
  private static final long serialVersionUID = MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT;
  public msg_nav_controller_output(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 26;
}

  /**
   * Current desired roll in degrees
   */
  public float nav_roll;
  /**
   * Current desired pitch in degrees
   */
  public float nav_pitch;
  /**
   * Current altitude error in meters
   */
  public float alt_error;
  /**
   * Current airspeed error in meters/second
   */
  public float aspd_error;
  /**
   * Current crosstrack error on x-y plane in meters
   */
  public float xtrack_error;
  /**
   * Current desired heading in degrees
   */
  public int nav_bearing;
  /**
   * Bearing to current MISSION/target in degrees
   */
  public int target_bearing;
  /**
   * Distance to active MISSION in meters
   */
  public int wp_dist;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  nav_roll = (float)dis.getFloat();
  nav_pitch = (float)dis.getFloat();
  alt_error = (float)dis.getFloat();
  aspd_error = (float)dis.getFloat();
  xtrack_error = (float)dis.getFloat();
  nav_bearing = (int)dis.getShort();
  target_bearing = (int)dis.getShort();
  wp_dist = (int)dis.getShort()&0x00FFFF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+26];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(nav_roll);
  dos.putFloat(nav_pitch);
  dos.putFloat(alt_error);
  dos.putFloat(aspd_error);
  dos.putFloat(xtrack_error);
  dos.putShort((short)(nav_bearing&0x00FFFF));
  dos.putShort((short)(target_bearing&0x00FFFF));
  dos.putShort((short)(wp_dist&0x00FFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 26);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[32] = crcl;
  buffer[33] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT : " +   "  nav_roll="+nav_roll+  "  nav_pitch="+nav_pitch+  "  alt_error="+alt_error+  "  aspd_error="+aspd_error+  "  xtrack_error="+xtrack_error+  "  nav_bearing="+nav_bearing+  "  target_bearing="+target_bearing+  "  wp_dist="+wp_dist;}
}
