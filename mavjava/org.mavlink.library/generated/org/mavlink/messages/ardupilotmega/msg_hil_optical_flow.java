/**
 * Generated class : msg_hil_optical_flow
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
 * Class msg_hil_optical_flow
 * Simulated optical flow from a flow sensor (e.g. optical mouse sensor)
 **/
public class msg_hil_optical_flow extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_HIL_OPTICAL_FLOW = 114;
  private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_OPTICAL_FLOW;
  public msg_hil_optical_flow(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_HIL_OPTICAL_FLOW;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 26;
}

  /**
   * Timestamp (UNIX)
   */
  public long time_usec;
  /**
   * Flow in meters in x-sensor direction, angular-speed compensated
   */
  public float flow_comp_m_x;
  /**
   * Flow in meters in y-sensor direction, angular-speed compensated
   */
  public float flow_comp_m_y;
  /**
   * Ground distance in meters. Positive value: distance known. Negative value: Unknown distance
   */
  public float ground_distance;
  /**
   * Flow in pixels in x-sensor direction
   */
  public int flow_x;
  /**
   * Flow in pixels in y-sensor direction
   */
  public int flow_y;
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
  flow_comp_m_x = (float)dis.readFloat();
  flow_comp_m_y = (float)dis.readFloat();
  ground_distance = (float)dis.readFloat();
  flow_x = (int)dis.readShort();
  flow_y = (int)dis.readShort();
  sensor_id = (int)dis.readUnsignedByte()&0x00FF;
  quality = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+26];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(time_usec);
  dos.writeFloat(flow_comp_m_x);
  dos.writeFloat(flow_comp_m_y);
  dos.writeFloat(ground_distance);
  dos.writeShort(flow_x&0x00FFFF);
  dos.writeShort(flow_y&0x00FFFF);
  dos.writeByte(sensor_id&0x00FF);
  dos.writeByte(quality&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 26);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[32] = crcl;
  buffer[33] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_HIL_OPTICAL_FLOW : " +   "  time_usec="+time_usec+  "  flow_comp_m_x="+flow_comp_m_x+  "  flow_comp_m_y="+flow_comp_m_y+  "  ground_distance="+ground_distance+  "  flow_x="+flow_x+  "  flow_y="+flow_y+  "  sensor_id="+sensor_id+  "  quality="+quality;}
}
