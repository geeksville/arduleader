/**
 * Generated class : msg_set_global_position_setpoint_int
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
 * Class msg_set_global_position_setpoint_int
 * Set the current global position setpoint.
 **/
public class msg_set_global_position_setpoint_int extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_GLOBAL_POSITION_SETPOINT_INT = 53;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_GLOBAL_POSITION_SETPOINT_INT;
  public msg_set_global_position_setpoint_int(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_GLOBAL_POSITION_SETPOINT_INT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 15;
}

  /**
   * WGS84 Latitude position in degrees * 1E7
   */
  public long latitude;
  /**
   * WGS84 Longitude position in degrees * 1E7
   */
  public long longitude;
  /**
   * WGS84 Altitude in meters * 1000 (positive for up)
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  latitude = (int)dis.readInt();
  longitude = (int)dis.readInt();
  altitude = (int)dis.readInt();
  yaw = (int)dis.readShort();
  coordinate_frame = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+15];
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
  dos.writeShort(yaw&0x00FFFF);
  dos.writeByte(coordinate_frame&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 15);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[21] = crcl;
  buffer[22] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_GLOBAL_POSITION_SETPOINT_INT : " +   "  latitude="+latitude+  "  longitude="+longitude+  "  altitude="+altitude+  "  yaw="+yaw+  "  coordinate_frame="+coordinate_frame;}
}
