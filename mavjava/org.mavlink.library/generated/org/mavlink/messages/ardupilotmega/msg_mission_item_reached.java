/**
 * Generated class : msg_mission_item_reached
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
 * Class msg_mission_item_reached
 * A certain mission item has been reached. The system will either hold this position (or circle on the orbit) or (if the autocontinue on the WP was set) continue to the next MISSION.
 **/
public class msg_mission_item_reached extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MISSION_ITEM_REACHED = 46;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_ITEM_REACHED;
  public msg_mission_item_reached(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MISSION_ITEM_REACHED;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 2;
}

  /**
   * Sequence
   */
  public int seq;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  seq = (int)dis.readUnsignedShort()&0x00FFFF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+2];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeShort(seq&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 2);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[8] = crcl;
  buffer[9] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MISSION_ITEM_REACHED : " +   "  seq="+seq;}
}
