/**
 * Generated class : msg_mission_item
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
 * Class msg_mission_item
 * Message encoding a mission item. This message is emitted to announce
                the presence of a mission item and to set a mission item on the system. The mission item can be either in x, y, z meters (type: LOCAL) or x:lat, y:lon, z:altitude. Local frame is Z-down, right handed (NED), global frame is Z-up, right handed (ENU). See also http://qgroundcontrol.org/mavlink/waypoint_protocol.
 **/
public class msg_mission_item extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MISSION_ITEM = 39;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MISSION_ITEM;
  public msg_mission_item(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MISSION_ITEM;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 37;
}

  /**
   * PARAM1 / For NAV command MISSIONs: Radius in which the MISSION is accepted as reached, in meters
   */
  public float param1;
  /**
   * PARAM2 / For NAV command MISSIONs: Time that the MAV should stay inside the PARAM1 radius before advancing, in milliseconds
   */
  public float param2;
  /**
   * PARAM3 / For LOITER command MISSIONs: Orbit to circle around the MISSION, in meters. If positive the orbit direction should be clockwise, if negative the orbit direction should be counter-clockwise.
   */
  public float param3;
  /**
   * PARAM4 / For NAV and LOITER command MISSIONs: Yaw orientation in degrees, [0..360] 0 = NORTH
   */
  public float param4;
  /**
   * PARAM5 / local: x position, global: latitude
   */
  public float x;
  /**
   * PARAM6 / y position: global: longitude
   */
  public float y;
  /**
   * PARAM7 / z position: global: altitude
   */
  public float z;
  /**
   * Sequence
   */
  public int seq;
  /**
   * The scheduled action for the MISSION. see MAV_CMD in common.xml MAVLink specs
   */
  public int command;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
  /**
   * The coordinate system of the MISSION. see MAV_FRAME in mavlink_types.h
   */
  public int frame;
  /**
   * false:0, true:1
   */
  public int current;
  /**
   * autocontinue to next wp
   */
  public int autocontinue;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  param1 = (float)dis.readFloat();
  param2 = (float)dis.readFloat();
  param3 = (float)dis.readFloat();
  param4 = (float)dis.readFloat();
  x = (float)dis.readFloat();
  y = (float)dis.readFloat();
  z = (float)dis.readFloat();
  seq = (int)dis.readUnsignedShort()&0x00FFFF;
  command = (int)dis.readUnsignedShort()&0x00FFFF;
  target_system = (int)dis.readUnsignedByte()&0x00FF;
  target_component = (int)dis.readUnsignedByte()&0x00FF;
  frame = (int)dis.readUnsignedByte()&0x00FF;
  current = (int)dis.readUnsignedByte()&0x00FF;
  autocontinue = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+37];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(param1);
  dos.writeFloat(param2);
  dos.writeFloat(param3);
  dos.writeFloat(param4);
  dos.writeFloat(x);
  dos.writeFloat(y);
  dos.writeFloat(z);
  dos.writeShort(seq&0x00FFFF);
  dos.writeShort(command&0x00FFFF);
  dos.writeByte(target_system&0x00FF);
  dos.writeByte(target_component&0x00FF);
  dos.writeByte(frame&0x00FF);
  dos.writeByte(current&0x00FF);
  dos.writeByte(autocontinue&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 37);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[43] = crcl;
  buffer[44] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MISSION_ITEM : " +   "  param1="+param1+  "  param2="+param2+  "  param3="+param3+  "  param4="+param4+  "  x="+x+  "  y="+y+  "  z="+z+  "  seq="+seq+  "  command="+command+  "  target_system="+target_system+  "  target_component="+target_component+  "  frame="+frame+  "  current="+current+  "  autocontinue="+autocontinue;}
}
