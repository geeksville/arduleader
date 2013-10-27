/**
 * Generated class : msg_omnidirectional_flow
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
 * Class msg_omnidirectional_flow
 * Optical flow from an omnidirectional flow sensor (e.g. PX4FLOW with wide angle lens)
 **/
public class msg_omnidirectional_flow extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW = 106;
  private static final long serialVersionUID = MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW;
  public msg_omnidirectional_flow(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 54;
}

  /**
   * Timestamp (microseconds, synced to UNIX time or since system boot)
   */
  public long time_usec;
  /**
   * Front distance in meters. Positive value (including zero): distance known. Negative value: Unknown distance
   */
  public float front_distance_m;
  /**
   * Flow in deci pixels (1 = 0.1 pixel) on left hemisphere
   */
  public int[] left = new int[10];
  /**
   * Flow in deci pixels (1 = 0.1 pixel) on right hemisphere
   */
  public int[] right = new int[10];
  /**
   * Sensor ID
   */
  public int sensor_id;
  /**
   * Optical flow quality / confidence. 0: bad, 255: maximum quality
   */
  public int quality;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (long)dis.getLong();
  front_distance_m = (float)dis.getFloat();
  for (int i=0; i<10; i++) {
    left[i] = (int)dis.getShort();
  }
  for (int i=0; i<10; i++) {
    right[i] = (int)dis.getShort();
  }
  sensor_id = (int)dis.get()&0x00FF;
  quality = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+54];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_usec);
  dos.putFloat(front_distance_m);
  for (int i=0; i<10; i++) {
    dos.putShort((short)(left[i]&0x00FFFF));
  }
  for (int i=0; i<10; i++) {
    dos.putShort((short)(right[i]&0x00FFFF));
  }
  dos.put((byte)(sensor_id&0x00FF));
  dos.put((byte)(quality&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 54);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[60] = crcl;
  buffer[61] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_OMNIDIRECTIONAL_FLOW : " +   "  time_usec="+time_usec+  "  front_distance_m="+front_distance_m+  "  left="+left+  "  right="+right+  "  sensor_id="+sensor_id+  "  quality="+quality;}
}
