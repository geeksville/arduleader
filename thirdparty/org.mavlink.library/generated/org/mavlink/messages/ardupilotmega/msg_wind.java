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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public void decode(ByteBuffer dis) throws IOException {
  direction = (float)dis.getFloat();
  speed = (float)dis.getFloat();
  speed_z = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+12];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(direction);
  dos.putFloat(speed);
  dos.putFloat(speed_z);
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
