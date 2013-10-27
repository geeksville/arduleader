/**
 * Generated class : msg_named_value_int
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
 * Class msg_named_value_int
 * Send a key-value pair as integer. The use of this message is discouraged for normal packets, but a quite efficient way for testing new messages and getting experimental debug output.
 **/
public class msg_named_value_int extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_NAMED_VALUE_INT = 252;
  private static final long serialVersionUID = MAVLINK_MSG_ID_NAMED_VALUE_INT;
  public msg_named_value_int(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_NAMED_VALUE_INT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 18;
}

  /**
   * Timestamp (milliseconds since system boot)
   */
  public long time_boot_ms;
  /**
   * Signed integer value
   */
  public long value;
  /**
   * Name of the debug variable
   */
  public char[] name = new char[10];
  public void setName(String tmp) {
    int len = Math.min(tmp.length(), 10);
    for (int i=0; i<len; i++) {
      name[i] = tmp.charAt(i);
    }
    for (int i=len; i<10; i++) {
      name[i] = 0;
    }
  }
  public String getName() {
    String result="";
    for (int i=0; i<10; i++) {
      if (name[i] != 0) result=result+name[i]; else break;
    }
    return result;
  }
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  value = (int)dis.getInt();
  for (int i=0; i<10; i++) {
    name[i] = (char)dis.get();
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+18];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.putInt((int)(value&0x00FFFFFFFF));
  for (int i=0; i<10; i++) {
    dos.put((byte)(name[i]));
  }
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 18);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[24] = crcl;
  buffer[25] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_NAMED_VALUE_INT : " +   "  time_boot_ms="+time_boot_ms+  "  value="+value+  "  name="+getName();}
}
