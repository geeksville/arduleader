/**
 * Generated class : msg_mount_status
 * DO NOT MODIFY!
 **/
package org.mavlink.messages.ardupilotmega;
import org.mavlink.messages.MAVLinkMessage;
import org.mavlink.IMAVLinkCRC;
import org.mavlink.MAVLinkCRC;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
/**
 * Class msg_mount_status
 * Message with some status from APM to GCS about camera or antenna mount
 **/
public class msg_mount_status extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MOUNT_STATUS = 158;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MOUNT_STATUS;
  public msg_mount_status(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MOUNT_STATUS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 14;
}

  /**
   * pitch(deg*100) or lat, depending on mount mode
   */
  public long pointing_a;
  /**
   * roll(deg*100) or lon depending on mount mode
   */
  public long pointing_b;
  /**
   * yaw(deg*100) or alt (in cm) depending on mount mode
   */
  public long pointing_c;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  pointing_a = (int)dis.readInt();
  pointing_b = (int)dis.readInt();
  pointing_c = (int)dis.readInt();
  target_system = (int)dis.readUnsignedByte()&0x00FF;
  target_component = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+14];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeInt((int)(pointing_a&0x00FFFFFFFF));
  dos.writeInt((int)(pointing_b&0x00FFFFFFFF));
  dos.writeInt((int)(pointing_c&0x00FFFFFFFF));
  dos.writeByte(target_system&0x00FF);
  dos.writeByte(target_component&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 14);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[20] = crcl;
  buffer[21] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MOUNT_STATUS : " +   "  pointing_a="+pointing_a+  "  pointing_b="+pointing_b+  "  pointing_c="+pointing_c+  "  target_system="+target_system+  "  target_component="+target_component;}
}
