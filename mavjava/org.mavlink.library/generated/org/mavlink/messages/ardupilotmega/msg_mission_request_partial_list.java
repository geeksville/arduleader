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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
/**
 * Class msg_mission_request_partial_list
 * Request a partial list of mission items from the system/component. http://qgroundcontrol.org/mavlink/waypoint_protocol. If start and end index are the same, just send one waypoint.
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
public void decode(ByteBuffer dis) throws IOException {
  start_index = (int)dis.getShort();
  end_index = (int)dis.getShort();
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+6];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(start_index&0x00FFFF));
  dos.putShort((short)(end_index&0x00FFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
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
