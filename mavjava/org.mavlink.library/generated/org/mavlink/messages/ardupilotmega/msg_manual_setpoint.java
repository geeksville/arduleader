/**
 * Generated class : msg_manual_setpoint
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
 * Class msg_manual_setpoint
 * Setpoint in roll, pitch, yaw and thrust from the operator
 **/
public class msg_manual_setpoint extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MANUAL_SETPOINT = 81;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MANUAL_SETPOINT;
  public msg_manual_setpoint(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MANUAL_SETPOINT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 22;
}

  /**
   * Timestamp in milliseconds since system boot
   */
  public long time_boot_ms;
  /**
   * Desired roll rate in radians per second
   */
  public float roll;
  /**
   * Desired pitch rate in radians per second
   */
  public float pitch;
  /**
   * Desired yaw rate in radians per second
   */
  public float yaw;
  /**
   * Collective thrust, normalized to 0 .. 1
   */
  public float thrust;
  /**
   * Flight mode switch position, 0.. 255
   */
  public int mode_switch;
  /**
   * Override mode switch position, 0.. 255
   */
  public int manual_override_switch;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  roll = (float)dis.getFloat();
  pitch = (float)dis.getFloat();
  yaw = (float)dis.getFloat();
  thrust = (float)dis.getFloat();
  mode_switch = (int)dis.get()&0x00FF;
  manual_override_switch = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+22];
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
  dos.put((byte)(mode_switch&0x00FF));
  dos.put((byte)(manual_override_switch&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 22);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[28] = crcl;
  buffer[29] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MANUAL_SETPOINT : " +   "  time_boot_ms="+time_boot_ms+  "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  thrust="+thrust+  "  mode_switch="+mode_switch+  "  manual_override_switch="+manual_override_switch;}
}
