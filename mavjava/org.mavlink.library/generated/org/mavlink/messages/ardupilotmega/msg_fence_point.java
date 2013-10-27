/**
 * Generated class : msg_fence_point
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
 * Class msg_fence_point
 * A fence point. Used to set a point when from
	      GCS -> MAV. Also used to return a point from MAV -> GCS
 **/
public class msg_fence_point extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_FENCE_POINT = 160;
  private static final long serialVersionUID = MAVLINK_MSG_ID_FENCE_POINT;
  public msg_fence_point(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_FENCE_POINT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 12;
}

  /**
   * Latitude of point
   */
  public float lat;
  /**
   * Longitude of point
   */
  public float lng;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
  /**
   * point index (first point is 1, 0 is for return point)
   */
  public int idx;
  /**
   * total number of points (for sanity checking)
   */
  public int count;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  lat = (float)dis.getFloat();
  lng = (float)dis.getFloat();
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  idx = (int)dis.get()&0x00FF;
  count = (int)dis.get()&0x00FF;
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
  dos.putFloat(lat);
  dos.putFloat(lng);
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  dos.put((byte)(idx&0x00FF));
  dos.put((byte)(count&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 12);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[18] = crcl;
  buffer[19] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_FENCE_POINT : " +   "  lat="+lat+  "  lng="+lng+  "  target_system="+target_system+  "  target_component="+target_component+  "  idx="+idx+  "  count="+count;}
}
