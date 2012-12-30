/**
 * Generated class : msg_memory_vect
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
 * Class msg_memory_vect
 * Send raw controller memory. The use of this message is discouraged for normal packets, but a quite efficient way for testing new messages and getting experimental debug output.
 **/
public class msg_memory_vect extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MEMORY_VECT = 249;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MEMORY_VECT;
  public msg_memory_vect(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MEMORY_VECT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 36;
}

  /**
   * Starting address of the debug variables
   */
  public int address;
  /**
   * Version code of the type variable. 0=unknown, type ignored and assumed int16_t. 1=as below
   */
  public int ver;
  /**
   * Type code of the memory variables. for ver = 1: 0=16 x int16_t, 1=16 x uint16_t, 2=16 x Q15, 3=16 x 1Q14
   */
  public int type;
  /**
   * Memory contents at specified address
   */
  public int[] value = new int[32];
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  address = (int)dis.readUnsignedShort()&0x00FFFF;
  ver = (int)dis.readUnsignedByte()&0x00FF;
  type = (int)dis.readUnsignedByte()&0x00FF;
  for (int i=0; i<32; i++) {
    value[i] = (int)dis.readByte();
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+36];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeShort(address&0x00FFFF);
  dos.writeByte(ver&0x00FF);
  dos.writeByte(type&0x00FF);
  for (int i=0; i<32; i++) {
    dos.write(value[i]&0x00FF);
  }
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 36);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[42] = crcl;
  buffer[43] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MEMORY_VECT : " +   "  address="+address+  "  ver="+ver+  "  type="+type+  "  value="+value;}
}
