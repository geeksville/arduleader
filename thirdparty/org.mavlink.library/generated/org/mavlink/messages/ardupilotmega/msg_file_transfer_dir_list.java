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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public void decode(ByteBuffer dis) throws IOException {
  transfer_uid = (long)dis.getLong();
  for (int i=0; i<240; i++) {
    dir_path[i] = (char)dis.get();
  }
  flags = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+249];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(transfer_uid);
  for (int i=0; i<240; i++) {
    dos.put((byte)(dir_path[i]));
  }
  dos.put((byte)(flags&0x00FF));
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
