/**
 * Generated class : msg_wind
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
 * Class msg_wind
 * Wind estimation
 **/
public class msg_wind extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_WIND = 168;
  private static final long serialVersionUID = MAVLINK_MSG_ID_WIND;
  public msg_wind(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_WIND;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 12;
}

  /**
   * wind direction that wind is coming from (degrees)
   */
  public float direction;
  /**
   * wind speed in ground plane (m/s)
   */
  public float speed;
  /**
   * vertical wind speed (m/s)
   */
  public float speed_z;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  direction = (float)dis.readFloat();
  speed = (float)dis.readFloat();
  speed_z = (float)dis.readFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+12];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(direction);
  dos.writeFloat(speed);
  dos.writeFloat(speed_z);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 12);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[18] = crcl;
  buffer[19] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_WIND : " +   "  direction="+direction+  "  speed="+speed+  "  speed_z="+speed_z;}
}
