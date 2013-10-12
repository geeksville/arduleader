/**
 * Generated class : msg_set_gps_global_origin
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
 * Class msg_set_gps_global_origin
 * As local waypoints exist, the global MISSION reference allows to transform between the local coordinate frame and the global (GPS) coordinate frame. This can be necessary when e.g. in- and outdoor settings are connected and the MAV should move from in- to outdoor.
 **/
public class msg_set_gps_global_origin extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_GPS_GLOBAL_ORIGIN = 48;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_GPS_GLOBAL_ORIGIN;
  public msg_set_gps_global_origin(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_GPS_GLOBAL_ORIGIN;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 13;
}

  /**
   * Latitude (WGS84), in degrees * 1E7
   */
  public long latitude;
  /**
   * Longitude (WGS84, in degrees * 1E7
   */
  public long longitude;
  /**
   * Altitude (WGS84), in meters * 1000 (positive for up)
   */
  public long altitude;
  /**
   * System ID
   */
  public int target_system;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  latitude = (int)dis.readInt();
  longitude = (int)dis.readInt();
  altitude = (int)dis.readInt();
  target_system = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+13];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeInt((int)(latitude&0x00FFFFFFFF));
  dos.writeInt((int)(longitude&0x00FFFFFFFF));
  dos.writeInt((int)(altitude&0x00FFFFFFFF));
  dos.writeByte(target_system&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 13);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[19] = crcl;
  buffer[20] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_GPS_GLOBAL_ORIGIN : " +   "  latitude="+latitude+  "  longitude="+longitude+  "  altitude="+altitude+  "  target_system="+target_system;}
}
