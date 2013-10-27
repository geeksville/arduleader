/**
 * Generated class : msg_roll_pitch_yaw_thrust_setpoint
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
 * Class msg_roll_pitch_yaw_thrust_setpoint
 * Setpoint in roll, pitch, yaw currently active on the system.
 **/
public class msg_roll_pitch_yaw_thrust_setpoint extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT = 58;
  private static final long serialVersionUID = MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT;
  public msg_roll_pitch_yaw_thrust_setpoint(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 20;
}

  /**
   * Timestamp in milliseconds since system boot
   */
  public long time_boot_ms;
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
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  roll = (float)dis.getFloat();
  pitch = (float)dis.getFloat();
  yaw = (float)dis.getFloat();
  thrust = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+20];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.putFloat(roll);
  dos.putFloat(pitch);
  dos.putFloat(yaw);
  dos.putFloat(thrust);
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 20);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[26] = crcl;
  buffer[27] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_ROLL_PITCH_YAW_THRUST_SETPOINT : " +   "  time_boot_ms="+time_boot_ms+  "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  thrust="+thrust;}
}
