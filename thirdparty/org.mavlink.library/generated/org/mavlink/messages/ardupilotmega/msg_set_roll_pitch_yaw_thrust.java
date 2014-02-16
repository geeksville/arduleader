/**
 * Generated class : msg_set_roll_pitch_yaw_thrust
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
 * Class msg_set_roll_pitch_yaw_thrust
 * Set roll, pitch and yaw.
 **/
public class msg_set_roll_pitch_yaw_thrust extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST = 56;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST;
  public msg_set_roll_pitch_yaw_thrust(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 18;
}

  /**
   * Desired roll angle in radians
   */
  public float roll;
  /**
   * Desired pitch angle in radians
   */
  public float pitch;
  /**
   * Desired yaw angle in radians
   */
  public float yaw;
  /**
   * Collective thrust, normalized to 0 .. 1
   */
  public float thrust;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  roll = (float)dis.getFloat();
  pitch = (float)dis.getFloat();
  yaw = (float)dis.getFloat();
  thrust = (float)dis.getFloat();
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+18];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(roll);
  dos.putFloat(pitch);
  dos.putFloat(yaw);
  dos.putFloat(thrust);
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 18);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[24] = crcl;
  buffer[25] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_THRUST : " +   "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  thrust="+thrust+  "  target_system="+target_system+  "  target_component="+target_component;}
}
