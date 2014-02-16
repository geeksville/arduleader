/**
 * Generated class : msg_statustext
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
 * Class msg_statustext
 * Status text message. These messages are printed in yellow in the COMM console of QGroundControl. WARNING: They consume quite some bandwidth, so use only for important status and error messages. If implemented wisely, these messages are buffered on the MCU and sent only at a limited rate (e.g. 10 Hz).
 **/
public class msg_statustext extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_STATUSTEXT = 253;
  private static final long serialVersionUID = MAVLINK_MSG_ID_STATUSTEXT;
  public msg_statustext(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_STATUSTEXT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 51;
}

  /**
   * Severity of status. Relies on the definitions within RFC-5424. See enum MAV_SEVERITY.
   */
  public int severity;
  /**
   * Status text message, without null termination character
   */
  public char[] text = new char[50];
  public void setText(String tmp) {
    int len = Math.min(tmp.length(), 50);
    for (int i=0; i<len; i++) {
      text[i] = tmp.charAt(i);
    }
    for (int i=len; i<50; i++) {
      text[i] = 0;
    }
  }
  public String getText() {
    String result="";
    for (int i=0; i<50; i++) {
      if (text[i] != 0) result=result+text[i]; else break;
    }
    return result;
  }
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  severity = (int)dis.get()&0x00FF;
  for (int i=0; i<50; i++) {
    text[i] = (char)dis.get();
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+51];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.put((byte)(severity&0x00FF));
  for (int i=0; i<50; i++) {
    dos.put((byte)(text[i]));
  }
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 51);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[57] = crcl;
  buffer[58] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_STATUSTEXT : " +   "  severity="+severity+  "  text="+getText();}
}
