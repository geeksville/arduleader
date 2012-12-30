/**
 * Generated class : msg_mission_request_partial_list
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
 * Class msg_mission_request_partial_list
 * Request the overall list of MISSIONs from the system/component. http://qgroundcontrol.org/mavlink/waypoint_protocol
 **/
public class msg_mission_request_partial_list extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST = 37;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST;
  public msg_mission_request_partial_list(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 6;
}

  /**
   * Start index, 0 by default
   */
  public int start_index;
  /**
   * End index, -1 by default (-1: send list to end). Else a valid index of the list
   */
  public int end_index;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  start_index = (int)dis.readShort();
  end_index = (int)dis.readShort();
  target_system = (int)dis.readUnsignedByte()&0x00FF;
  target_component = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+6];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeShort(start_index&0x00FFFF);
  dos.writeShort(end_index&0x00FFFF);
  dos.writeByte(target_system&0x00FF);
  dos.writeByte(target_component&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 6);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[12] = crcl;
  buffer[13] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MISSION_REQUEST_PARTIAL_LIST : " +   "  start_index="+start_index+  "  end_index="+end_index+  "  target_system="+target_system+  "  target_component="+target_component;}
}
