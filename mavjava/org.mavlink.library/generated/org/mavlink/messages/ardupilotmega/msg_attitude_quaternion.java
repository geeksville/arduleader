/**
 * Generated class : msg_attitude_quaternion
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
 * Class msg_attitude_quaternion
 * The attitude in the aeronautical frame (right-handed, Z-down, X-front, Y-right), expressed as quaternion.
 **/
public class msg_attitude_quaternion extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_ATTITUDE_QUATERNION = 31;
  private static final long serialVersionUID = MAVLINK_MSG_ID_ATTITUDE_QUATERNION;
  public msg_attitude_quaternion(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_ATTITUDE_QUATERNION;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 32;
}

  /**
   * Timestamp (milliseconds since system boot)
   */
  public long time_boot_ms;
  /**
   * Quaternion component 1
   */
  public float q1;
  /**
   * Quaternion component 2
   */
  public float q2;
  /**
   * Quaternion component 3
   */
  public float q3;
  /**
   * Quaternion component 4
   */
  public float q4;
  /**
   * Roll angular speed (rad/s)
   */
  public float rollspeed;
  /**
   * Pitch angular speed (rad/s)
   */
  public float pitchspeed;
  /**
   * Yaw angular speed (rad/s)
   */
  public float yawspeed;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  q1 = (float)dis.getFloat();
  q2 = (float)dis.getFloat();
  q3 = (float)dis.getFloat();
  q4 = (float)dis.getFloat();
  rollspeed = (float)dis.getFloat();
  pitchspeed = (float)dis.getFloat();
  yawspeed = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+32];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.putFloat(q1);
  dos.putFloat(q2);
  dos.putFloat(q3);
  dos.putFloat(q4);
  dos.putFloat(rollspeed);
  dos.putFloat(pitchspeed);
  dos.putFloat(yawspeed);
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 32);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[38] = crcl;
  buffer[39] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_ATTITUDE_QUATERNION : " +   "  time_boot_ms="+time_boot_ms+  "  q1="+q1+  "  q2="+q2+  "  q3="+q3+  "  q4="+q4+  "  rollspeed="+rollspeed+  "  pitchspeed="+pitchspeed+  "  yawspeed="+yawspeed;}
}
