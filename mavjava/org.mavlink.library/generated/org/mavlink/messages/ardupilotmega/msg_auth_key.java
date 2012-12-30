/**
 * Generated class : msg_auth_key
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
 * Class msg_auth_key
 * Emit an encrypted signature / key identifying this system. PLEASE NOTE: This protocol has been kept simple, so transmitting the key requires an encrypted channel for true safety.
 **/
public class msg_auth_key extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_AUTH_KEY = 7;
  private static final long serialVersionUID = MAVLINK_MSG_ID_AUTH_KEY;
  public msg_auth_key(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_AUTH_KEY;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 32;
}

  /**
   * key
   */
  public char[] key = new char[32];
  public void setKey(String tmp) {
    int len = Math.min(tmp.length(), 32);
    for (int i=0; i<len; i++) {
      key[i] = tmp.charAt(i);
    }
    for (int i=len; i<32; i++) {
      key[i] = 0;
    }
  }
  public String getKey() {
    String result="";
    for (int i=0; i<32; i++) {
      if (key[i] != 0) result=result+key[i]; else break;
    }
    return result;
  }
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  for (int i=0; i<32; i++) {
    key[i] = (char)dis.readByte();
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+32];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  for (int i=0; i<32; i++) {
    dos.writeByte(key[i]);
  }
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 32);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[38] = crcl;
  buffer[39] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_AUTH_KEY : " +   "  key="+getKey();}
}
