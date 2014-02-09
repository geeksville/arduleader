/**
 * Generated class : msg_file_transfer_res
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
 * Class msg_file_transfer_res
 * File transfer result
 **/
public class msg_file_transfer_res extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_FILE_TRANSFER_RES = 112;
  private static final long serialVersionUID = MAVLINK_MSG_ID_FILE_TRANSFER_RES;
  public msg_file_transfer_res(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_FILE_TRANSFER_RES;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 9;
}

  /**
   * Unique transfer ID
   */
  public long transfer_uid;
  /**
   * 0: OK, 1: not permitted, 2: bad path / file name, 3: no space left on device
   */
  public int result;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  transfer_uid = (long)dis.getLong();
  result = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+9];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(transfer_uid);
  dos.put((byte)(result&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 9);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[15] = crcl;
  buffer[16] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_FILE_TRANSFER_RES : " +   "  transfer_uid="+transfer_uid+  "  result="+result;}
}
