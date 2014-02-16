/**
 * Generated class : msg_debug
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
 * Class msg_debug
 * Send a debug value. The index is used to discriminate between values. These values show up in the plot of QGroundControl as DEBUG N.
 **/
public class msg_debug extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_DEBUG = 254;
  private static final long serialVersionUID = MAVLINK_MSG_ID_DEBUG;
  public msg_debug(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_DEBUG;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 9;
}

  /**
   * Timestamp (milliseconds since system boot)
   */
  public long time_boot_ms;
  /**
   * DEBUG value
   */
  public float value;
  /**
   * index of debug variable
   */
  public int ind;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  value = (float)dis.getFloat();
  ind = (int)dis.get()&0x00FF;
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
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.putFloat(value);
  dos.put((byte)(ind&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 9);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[15] = crcl;
  buffer[16] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_DEBUG : " +   "  time_boot_ms="+time_boot_ms+  "  value="+value+  "  ind="+ind;}
}
