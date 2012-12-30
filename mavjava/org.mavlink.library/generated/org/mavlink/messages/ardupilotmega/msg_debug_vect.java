/**
 * Generated class : msg_debug_vect
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
 * Class msg_debug_vect
 * 
 **/
public class msg_debug_vect extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_DEBUG_VECT = 250;
  private static final long serialVersionUID = MAVLINK_MSG_ID_DEBUG_VECT;
  public msg_debug_vect(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_DEBUG_VECT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 30;
}

  /**
   * Timestamp
   */
  public long time_usec;
  /**
   * x
   */
  public float x;
  /**
   * y
   */
  public float y;
  /**
   * z
   */
  public float z;
  /**
   * Name
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_usec = (long)dis.readLong();
  x = (float)dis.readFloat();
  y = (float)dis.readFloat();
  z = (float)dis.readFloat();
  for (int i=0; i<10; i++) {
    name[i] = (char)dis.readByte();
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+30];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(time_usec);
  dos.writeFloat(x);
  dos.writeFloat(y);
  dos.writeFloat(z);
  for (int i=0; i<10; i++) {
    dos.writeByte(name[i]);
  }
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 30);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[36] = crcl;
  buffer[37] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_DEBUG_VECT : " +   "  time_usec="+time_usec+  "  x="+x+  "  y="+y+  "  z="+z+  "  name="+getName();}
}
