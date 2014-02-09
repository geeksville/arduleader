/**
 * Generated class : msg_mount_control
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
 * Class msg_mount_control
 * Message to control a camera mount, directional antenna, etc.
 **/
public class msg_mount_control extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MOUNT_CONTROL = 157;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MOUNT_CONTROL;
  public msg_mount_control(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MOUNT_CONTROL;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 15;
}

  /**
   * pitch(deg*100) or lat, depending on mount mode
   */
  public long input_a;
  /**
   * roll(deg*100) or lon depending on mount mode
   */
  public long input_b;
  /**
   * yaw(deg*100) or alt (in cm) depending on mount mode
   */
  public long input_c;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
  /**
   * if "1" it will save current trimmed position on EEPROM (just valid for NEUTRAL and LANDING)
   */
  public int save_position;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  input_a = (int)dis.getInt();
  input_b = (int)dis.getInt();
  input_c = (int)dis.getInt();
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  save_position = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+15];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(input_a&0x00FFFFFFFF));
  dos.putInt((int)(input_b&0x00FFFFFFFF));
  dos.putInt((int)(input_c&0x00FFFFFFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  dos.put((byte)(save_position&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 15);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[21] = crcl;
  buffer[22] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MOUNT_CONTROL : " +   "  input_a="+input_a+  "  input_b="+input_b+  "  input_c="+input_c+  "  target_system="+target_system+  "  target_component="+target_component+  "  save_position="+save_position;}
}
