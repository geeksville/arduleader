/**
 * Generated class : msg_hwstatus
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
 * Class msg_hwstatus
 * Status of key hardware
 **/
public class msg_hwstatus extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_HWSTATUS = 165;
  private static final long serialVersionUID = MAVLINK_MSG_ID_HWSTATUS;
  public msg_hwstatus(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_HWSTATUS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 3;
}

  /**
   * board voltage (mV)
   */
  public int Vcc;
  /**
   * I2C error count
   */
  public int I2Cerr;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  Vcc = (int)dis.readUnsignedShort()&0x00FFFF;
  I2Cerr = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+3];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeShort(Vcc&0x00FFFF);
  dos.writeByte(I2Cerr&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 3);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[9] = crcl;
  buffer[10] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_HWSTATUS : " +   "  Vcc="+Vcc+  "  I2Cerr="+I2Cerr;}
}
