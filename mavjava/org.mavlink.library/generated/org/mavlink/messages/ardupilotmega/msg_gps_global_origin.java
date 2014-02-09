/**
 * Generated class : msg_gps_global_origin
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
 * Class msg_gps_global_origin
 * Once the MAV sets a new GPS-Local correspondence, this message announces the origin (0,0,0) position
 **/
public class msg_gps_global_origin extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN = 49;
  private static final long serialVersionUID = MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN;
  public msg_gps_global_origin(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 12;
}

  /**
   * Latitude (WGS84), in degrees * 1E7
   */
  public long latitude;
  /**
   * Longitude (WGS84), in degrees * 1E7
   */
  public long longitude;
  /**
   * Altitude (WGS84), in meters * 1000 (positive for up)
   */
  public long altitude;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  latitude = (int)dis.getInt();
  longitude = (int)dis.getInt();
  altitude = (int)dis.getInt();
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
  dos.putInt((int)(latitude&0x00FFFFFFFF));
  dos.putInt((int)(longitude&0x00FFFFFFFF));
  dos.putInt((int)(altitude&0x00FFFFFFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 12);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[18] = crcl;
  buffer[19] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_GPS_GLOBAL_ORIGIN : " +   "  latitude="+latitude+  "  longitude="+longitude+  "  altitude="+altitude;}
}
