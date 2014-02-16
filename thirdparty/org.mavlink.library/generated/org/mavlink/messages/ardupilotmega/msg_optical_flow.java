/**
 * Generated class : msg_optical_flow
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
 * Class msg_optical_flow
 * Optical flow from a flow sensor (e.g. optical mouse sensor)
 **/
public class msg_optical_flow extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_OPTICAL_FLOW = 100;
  private static final long serialVersionUID = MAVLINK_MSG_ID_OPTICAL_FLOW;
  public msg_optical_flow(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_OPTICAL_FLOW;
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
   * Flow in pixels * 10 in x-sensor direction (dezi-pixels)
   */
  public int flow_x;
  /**
   * Flow in pixels * 10 in y-sensor direction (dezi-pixels)
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
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (long)dis.getLong();
  flow_comp_m_x = (float)dis.getFloat();
  flow_comp_m_y = (float)dis.getFloat();
  ground_distance = (float)dis.getFloat();
  flow_x = (int)dis.getShort();
  flow_y = (int)dis.getShort();
  sensor_id = (int)dis.get()&0x00FF;
  quality = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+26];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_usec);
  dos.putFloat(flow_comp_m_x);
  dos.putFloat(flow_comp_m_y);
  dos.putFloat(ground_distance);
  dos.putShort((short)(flow_x&0x00FFFF));
  dos.putShort((short)(flow_y&0x00FFFF));
  dos.put((byte)(sensor_id&0x00FF));
  dos.put((byte)(quality&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 26);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[32] = crcl;
  buffer[33] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_OPTICAL_FLOW : " +   "  time_usec="+time_usec+  "  flow_comp_m_x="+flow_comp_m_x+  "  flow_comp_m_y="+flow_comp_m_y+  "  ground_distance="+ground_distance+  "  flow_x="+flow_x+  "  flow_y="+flow_y+  "  sensor_id="+sensor_id+  "  quality="+quality;}
}
