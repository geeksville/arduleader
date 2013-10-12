/**
 * Generated class : msg_data96
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
 * Class msg_data96
 * Data packet, size 96
 **/
public class msg_data96 extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_DATA96 = 172;
  private static final long serialVersionUID = MAVLINK_MSG_ID_DATA96;
  public msg_data96(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_DATA96;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 98;
}

  /**
   * data type
   */
  public int type;
  /**
   * data length
   */
  public int len;
  /**
   * raw data
   */
  public int[] data = new int[96];
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  type = (int)dis.readUnsignedByte()&0x00FF;
  len = (int)dis.readUnsignedByte()&0x00FF;
  for (int i=0; i<96; i++) {
    data[i] = (int)dis.readUnsignedByte()&0x00FF;
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+98];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeByte(type&0x00FF);
  dos.writeByte(len&0x00FF);
  for (int i=0; i<96; i++) {
    dos.writeByte(data[i]&0x00FF);
  }
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 98);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[104] = crcl;
  buffer[105] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_DATA96 : " +   "  type="+type+  "  len="+len+  "  data="+data;}
}
