/**
 * Generated class : msg_set_mag_offsets
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
 * Class msg_set_mag_offsets
 * set the magnetometer offsets
 **/
public class msg_set_mag_offsets extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_MAG_OFFSETS = 151;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_MAG_OFFSETS;
  public msg_set_mag_offsets(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_MAG_OFFSETS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 8;
}

  /**
   * magnetometer X offset
   */
  public int mag_ofs_x;
  /**
   * magnetometer Y offset
   */
  public int mag_ofs_y;
  /**
   * magnetometer Z offset
   */
  public int mag_ofs_z;
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
  mag_ofs_x = (int)dis.getShort();
  mag_ofs_y = (int)dis.getShort();
  mag_ofs_z = (int)dis.getShort();
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+8];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(mag_ofs_x&0x00FFFF));
  dos.putShort((short)(mag_ofs_y&0x00FFFF));
  dos.putShort((short)(mag_ofs_z&0x00FFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 8);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[14] = crcl;
  buffer[15] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_MAG_OFFSETS : " +   "  mag_ofs_x="+mag_ofs_x+  "  mag_ofs_y="+mag_ofs_y+  "  mag_ofs_z="+mag_ofs_z+  "  target_system="+target_system+  "  target_component="+target_component;}
}
