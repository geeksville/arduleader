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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_usec = (long)dis.readLong();
  front_distance_m = (float)dis.readFloat();
  for (int i=0; i<10; i++) {
    left[i] = (int)dis.readShort();
  }
  for (int i=0; i<10; i++) {
    right[i] = (int)dis.readShort();
  }
  sensor_id = (int)dis.readUnsignedByte()&0x00FF;
  quality = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+54];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(time_usec);
  dos.writeFloat(front_distance_m);
  for (int i=0; i<10; i++) {
    dos.writeShort(left[i]&0x00FFFF);
  }
  for (int i=0; i<10; i++) {
    dos.writeShort(right[i]&0x00FFFF);
  }
  dos.writeByte(sensor_id&0x00FF);
  dos.writeByte(quality&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
