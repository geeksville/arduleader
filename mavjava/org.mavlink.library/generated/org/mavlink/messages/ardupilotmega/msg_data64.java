/**
 * Generated class : msg_data64
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
 * Class msg_data64
 * Data packet, size 64
 **/
public class msg_data64 extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_DATA64 = 171;
  private static final long serialVersionUID = MAVLINK_MSG_ID_DATA64;
  public msg_data64(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_DATA64;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 66;
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
  public int[] data = new int[64];
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  type = (int)dis.get()&0x00FF;
  len = (int)dis.get()&0x00FF;
  for (int i=0; i<64; i++) {
    data[i] = (int)dis.get()&0x00FF;
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+66];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.put((byte)(type&0x00FF));
  dos.put((byte)(len&0x00FF));
  for (int i=0; i<64; i++) {
    dos.put((byte)(data[i]&0x00FF));
  }
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 66);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[72] = crcl;
  buffer[73] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_DATA64 : " +   "  type="+type+  "  len="+len+  "  data="+data;}
}
