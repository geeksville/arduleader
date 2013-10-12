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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  transfer_uid = (long)dis.readLong();
  result = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+9];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(transfer_uid);
  dos.writeByte(result&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
