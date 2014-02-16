/**
 * Generated class : msg_mission_ack
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
 * Class msg_mission_ack
 * Ack message during MISSION handling. The type field states if this message is a positive ack (type=0) or if an error happened (type=non-zero).
 **/
public class msg_mission_ack extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MISSION_ACK = 47;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_ACK;
  public msg_mission_ack(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MISSION_ACK;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 3;
}

  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
  /**
   * See MAV_MISSION_RESULT enum
   */
  public int type;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  type = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+3];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  dos.put((byte)(type&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 3);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[9] = crcl;
  buffer[10] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MISSION_ACK : " +   "  target_system="+target_system+  "  target_component="+target_component+  "  type="+type;}
}
