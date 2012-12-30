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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  lat = (float)dis.readFloat();
  lng = (float)dis.readFloat();
  target_system = (int)dis.readUnsignedByte()&0x00FF;
  target_component = (int)dis.readUnsignedByte()&0x00FF;
  idx = (int)dis.readUnsignedByte()&0x00FF;
  count = (int)dis.readUnsignedByte()&0x00FF;
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
  dos.writeFloat(lat);
  dos.writeFloat(lng);
  dos.writeByte(target_system&0x00FF);
  dos.writeByte(target_component&0x00FF);
  dos.writeByte(idx&0x00FF);
  dos.writeByte(count&0x00FF);
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
return "MAVLINK_MSG_ID_FENCE_POINT : " +   "  lat="+lat+  "  lng="+lng+  "  target_system="+target_system+  "  target_component="+target_component+  "  idx="+idx+  "  count="+count;}
}
