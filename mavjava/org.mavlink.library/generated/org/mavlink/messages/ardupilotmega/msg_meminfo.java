/**
 * Generated class : msg_meminfo
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
 * Class msg_meminfo
 * state of APM memory
 **/
public class msg_meminfo extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MEMINFO = 152;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MEMINFO;
  public msg_meminfo(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MEMINFO;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 4;
}

  /**
   * heap top
   */
  public int brkval;
  /**
   * free memory
   */
  public int freemem;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  brkval = (int)dis.getShort()&0x00FFFF;
  freemem = (int)dis.getShort()&0x00FFFF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+4];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(brkval&0x00FFFF));
  dos.putShort((short)(freemem&0x00FFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 4);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[10] = crcl;
  buffer[11] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MEMINFO : " +   "  brkval="+brkval+  "  freemem="+freemem;}
}
