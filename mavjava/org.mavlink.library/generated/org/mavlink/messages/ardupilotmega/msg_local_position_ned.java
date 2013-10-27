/**
 * Generated class : msg_local_position_ned
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
 * Class msg_local_position_ned
 * The filtered local position (e.g. fused computer vision and accelerometers). Coordinate frame is right-handed, Z-axis down (aeronautical frame, NED / north-east-down convention)
 **/
public class msg_local_position_ned extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_LOCAL_POSITION_NED = 32;
  private static final long serialVersionUID = MAVLINK_MSG_ID_LOCAL_POSITION_NED;
  public msg_local_position_ned(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_LOCAL_POSITION_NED;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 28;
}

  /**
   * Timestamp (milliseconds since system boot)
   */
  public long time_boot_ms;
  /**
   * X Position
   */
  public float x;
  /**
   * Y Position
   */
  public float y;
  /**
   * Z Position
   */
  public float z;
  /**
   * X Speed
   */
  public float vx;
  /**
   * Y Speed
   */
  public float vy;
  /**
   * Z Speed
   */
  public float vz;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  x = (float)dis.getFloat();
  y = (float)dis.getFloat();
  z = (float)dis.getFloat();
  vx = (float)dis.getFloat();
  vy = (float)dis.getFloat();
  vz = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+28];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.putFloat(x);
  dos.putFloat(y);
  dos.putFloat(z);
  dos.putFloat(vx);
  dos.putFloat(vy);
  dos.putFloat(vz);
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 28);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[34] = crcl;
  buffer[35] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_LOCAL_POSITION_NED : " +   "  time_boot_ms="+time_boot_ms+  "  x="+x+  "  y="+y+  "  z="+z+  "  vx="+vx+  "  vy="+vy+  "  vz="+vz;}
}
