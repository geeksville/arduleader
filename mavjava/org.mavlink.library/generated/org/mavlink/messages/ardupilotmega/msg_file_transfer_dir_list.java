/**
 * Generated class : msg_file_transfer_dir_list
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
 * Class msg_file_transfer_dir_list
 * Get directory listing
 **/
public class msg_file_transfer_dir_list extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_FILE_TRANSFER_DIR_LIST = 111;
  private static final long serialVersionUID = MAVLINK_MSG_ID_FILE_TRANSFER_DIR_LIST;
  public msg_file_transfer_dir_list(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_FILE_TRANSFER_DIR_LIST;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 249;
}

  /**
   * Unique transfer ID
   */
  public long transfer_uid;
  /**
   * Directory path to list
   */
  public char[] dir_path = new char[240];
  public void setDir_path(String tmp) {
    int len = Math.min(tmp.length(), 240);
    for (int i=0; i<len; i++) {
      dir_path[i] = tmp.charAt(i);
    }
    for (int i=len; i<240; i++) {
      dir_path[i] = 0;
    }
  }
  public String getDir_path() {
    String result="";
    for (int i=0; i<240; i++) {
      if (dir_path[i] != 0) result=result+dir_path[i]; else break;
    }
    return result;
  }
  /**
   * RESERVED
   */
  public int flags;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  transfer_uid = (long)dis.readLong();
  for (int i=0; i<240; i++) {
    dir_path[i] = (char)dis.readByte();
  }
  flags = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+249];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(transfer_uid);
  for (int i=0; i<240; i++) {
    dos.writeByte(dir_path[i]);
  }
  dos.writeByte(flags&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 249);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[255] = crcl;
  buffer[256] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_FILE_TRANSFER_DIR_LIST : " +   "  transfer_uid="+transfer_uid+  "  dir_path="+getDir_path()+  "  flags="+flags;}
}
