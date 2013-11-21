/**
 * Generated class : msg_rally_point
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
 * Class msg_rally_point
 * A rally point. Used to set a point when from GCS -> MAV. Also used to return a point from MAV -> GCS
 **/
public class msg_rally_point extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_RALLY_POINT = 175;
  private static final long serialVersionUID = MAVLINK_MSG_ID_RALLY_POINT;
  public msg_rally_point(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_RALLY_POINT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 19;
}

  /**
   * Latitude of point in degrees * 1E7
   */
  public long lat;
  /**
   * Longitude of point in degrees * 1E7
   */
  public long lng;
  /**
   * Transit / loiter altitude in meters relative to home
   */
  public int alt;
  /**
   * Break altitude in meters relative to home
   */
  public int break_alt;
  /**
   * Heading to aim for when landing. In centi-degrees.
   */
  public int land_dir;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
  /**
   * point index (first point is 0)
   */
  public int idx;
  /**
   * total number of points (for sanity checking)
   */
  public int count;
  /**
   * See RALLY_FLAGS enum for definition of the bitmask.
   */
  public int flags;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  lat = (int)dis.getInt();
  lng = (int)dis.getInt();
  alt = (int)dis.getShort();
  break_alt = (int)dis.getShort();
  land_dir = (int)dis.getShort()&0x00FFFF;
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  idx = (int)dis.get()&0x00FF;
  count = (int)dis.get()&0x00FF;
  flags = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+19];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(lat&0x00FFFFFFFF));
  dos.putInt((int)(lng&0x00FFFFFFFF));
  dos.putShort((short)(alt&0x00FFFF));
  dos.putShort((short)(break_alt&0x00FFFF));
  dos.putShort((short)(land_dir&0x00FFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  dos.put((byte)(idx&0x00FF));
  dos.put((byte)(count&0x00FF));
  dos.put((byte)(flags&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 19);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[25] = crcl;
  buffer[26] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_RALLY_POINT : " +   "  lat="+lat+  "  lng="+lng+  "  alt="+alt+  "  break_alt="+break_alt+  "  land_dir="+land_dir+  "  target_system="+target_system+  "  target_component="+target_component+  "  idx="+idx+  "  count="+count+  "  flags="+flags;}
}
