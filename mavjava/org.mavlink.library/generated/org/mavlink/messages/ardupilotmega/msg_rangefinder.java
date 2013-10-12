/**
 * Generated class : msg_rangefinder
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
 * Class msg_rangefinder
 * Rangefinder reporting
 **/
public class msg_rangefinder extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_RANGEFINDER = 173;
  private static final long serialVersionUID = MAVLINK_MSG_ID_RANGEFINDER;
  public msg_rangefinder(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_RANGEFINDER;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 8;
}

  /**
   * distance in meters
   */
  public float distance;
  /**
   * raw voltage if available, zero otherwise
   */
  public float voltage;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  distance = (float)dis.readFloat();
  voltage = (float)dis.readFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+8];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(distance);
  dos.writeFloat(voltage);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 8);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[14] = crcl;
  buffer[15] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_RANGEFINDER : " +   "  distance="+distance+  "  voltage="+voltage;}
}
