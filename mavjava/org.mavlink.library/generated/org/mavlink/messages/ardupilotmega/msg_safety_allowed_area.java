/**
 * Generated class : msg_safety_allowed_area
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
 * Class msg_safety_allowed_area
 * Read out the safety zone the MAV currently assumes.
 **/
public class msg_safety_allowed_area extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SAFETY_ALLOWED_AREA = 55;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SAFETY_ALLOWED_AREA;
  public msg_safety_allowed_area(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SAFETY_ALLOWED_AREA;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 25;
}

  /**
   * x position 1 / Latitude 1
   */
  public float p1x;
  /**
   * y position 1 / Longitude 1
   */
  public float p1y;
  /**
   * z position 1 / Altitude 1
   */
  public float p1z;
  /**
   * x position 2 / Latitude 2
   */
  public float p2x;
  /**
   * y position 2 / Longitude 2
   */
  public float p2y;
  /**
   * z position 2 / Altitude 2
   */
  public float p2z;
  /**
   * Coordinate frame, as defined by MAV_FRAME enum in mavlink_types.h. Can be either global, GPS, right-handed with Z axis up or local, right handed, Z axis down.
   */
  public int frame;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  p1x = (float)dis.getFloat();
  p1y = (float)dis.getFloat();
  p1z = (float)dis.getFloat();
  p2x = (float)dis.getFloat();
  p2y = (float)dis.getFloat();
  p2z = (float)dis.getFloat();
  frame = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+25];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(p1x);
  dos.putFloat(p1y);
  dos.putFloat(p1z);
  dos.putFloat(p2x);
  dos.putFloat(p2y);
  dos.putFloat(p2z);
  dos.put((byte)(frame&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 25);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[31] = crcl;
  buffer[32] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SAFETY_ALLOWED_AREA : " +   "  p1x="+p1x+  "  p1y="+p1y+  "  p1z="+p1z+  "  p2x="+p2x+  "  p2y="+p2y+  "  p2z="+p2z+  "  frame="+frame;}
}
