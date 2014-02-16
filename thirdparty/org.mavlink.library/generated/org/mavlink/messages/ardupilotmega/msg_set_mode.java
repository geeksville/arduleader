/**
 * Generated class : msg_set_mode
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
 * Class msg_set_mode
 * Set the system mode, as defined by enum MAV_MODE. There is no target component id as the mode is by definition for the overall aircraft, not only for one component.
 **/
public class msg_set_mode extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_MODE = 11;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_MODE;
  public msg_set_mode(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_MODE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 6;
}

  /**
   * The new autopilot-specific mode. This field can be ignored by an autopilot.
   */
  public long custom_mode;
  /**
   * The system setting the mode
   */
  public int target_system;
  /**
   * The new base mode
   */
  public int base_mode;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  custom_mode = (int)dis.getInt()&0x00FFFFFFFF;
  target_system = (int)dis.get()&0x00FF;
  base_mode = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+6];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(custom_mode&0x00FFFFFFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(base_mode&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 6);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[12] = crcl;
  buffer[13] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_MODE : " +   "  custom_mode="+custom_mode+  "  target_system="+target_system+  "  base_mode="+base_mode;}
}
