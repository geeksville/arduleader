/**
 * Generated class : msg_global_position_setpoint_int
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
 * Class msg_global_position_setpoint_int
 * Transmit the current local setpoint of the controller to other MAVs (collision avoidance) and to the GCS.
 **/
public class msg_global_position_setpoint_int extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_GLOBAL_POSITION_SETPOINT_INT = 52;
  private static final long serialVersionUID = MAVLINK_MSG_ID_GLOBAL_POSITION_SETPOINT_INT;
  public msg_global_position_setpoint_int(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_GLOBAL_POSITION_SETPOINT_INT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 15;
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
   * Desired yaw angle in degrees * 100
   */
  public int yaw;
  /**
   * Coordinate frame - valid values are only MAV_FRAME_GLOBAL or MAV_FRAME_GLOBAL_RELATIVE_ALT
   */
  public int coordinate_frame;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  latitude = (int)dis.getInt();
  longitude = (int)dis.getInt();
  altitude = (int)dis.getInt();
  yaw = (int)dis.getShort();
  coordinate_frame = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+15];
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
  dos.putShort((short)(yaw&0x00FFFF));
  dos.put((byte)(coordinate_frame&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 15);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[21] = crcl;
  buffer[22] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_GLOBAL_POSITION_SETPOINT_INT : " +   "  latitude="+latitude+  "  longitude="+longitude+  "  altitude="+altitude+  "  yaw="+yaw+  "  coordinate_frame="+coordinate_frame;}
}
