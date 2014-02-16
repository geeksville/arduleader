/**
 * Generated class : msg_vicon_position_estimate
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
 * Class msg_vicon_position_estimate
 * 
 **/
public class msg_vicon_position_estimate extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_VICON_POSITION_ESTIMATE = 104;
  private static final long serialVersionUID = MAVLINK_MSG_ID_VICON_POSITION_ESTIMATE;
  public msg_vicon_position_estimate(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_VICON_POSITION_ESTIMATE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 32;
}

  /**
   * Timestamp (microseconds, synced to UNIX time or since system boot)
   */
  public long usec;
  /**
   * Global X position
   */
  public float x;
  /**
   * Global Y position
   */
  public float y;
  /**
   * Global Z position
   */
  public float z;
  /**
   * Roll angle in rad
   */
  public float roll;
  /**
   * Pitch angle in rad
   */
  public float pitch;
  /**
   * Yaw angle in rad
   */
  public float yaw;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  usec = (long)dis.getLong();
  x = (float)dis.getFloat();
  y = (float)dis.getFloat();
  z = (float)dis.getFloat();
  roll = (float)dis.getFloat();
  pitch = (float)dis.getFloat();
  yaw = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+32];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(usec);
  dos.putFloat(x);
  dos.putFloat(y);
  dos.putFloat(z);
  dos.putFloat(roll);
  dos.putFloat(pitch);
  dos.putFloat(yaw);
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 32);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[38] = crcl;
  buffer[39] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_VICON_POSITION_ESTIMATE : " +   "  usec="+usec+  "  x="+x+  "  y="+y+  "  z="+z+  "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw;}
}
