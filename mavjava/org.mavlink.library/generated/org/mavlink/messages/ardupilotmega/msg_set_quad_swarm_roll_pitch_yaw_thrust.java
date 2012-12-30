/**
 * Generated class : msg_set_quad_swarm_roll_pitch_yaw_thrust
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
 * Class msg_set_quad_swarm_roll_pitch_yaw_thrust
 * Setpoint for up to four quadrotors in a group / wing
 **/
public class msg_set_quad_swarm_roll_pitch_yaw_thrust extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_QUAD_SWARM_ROLL_PITCH_YAW_THRUST = 61;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_QUAD_SWARM_ROLL_PITCH_YAW_THRUST;
  public msg_set_quad_swarm_roll_pitch_yaw_thrust(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_QUAD_SWARM_ROLL_PITCH_YAW_THRUST;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 34;
}

  /**
   * Desired roll angle in radians +-PI (+-32767)
   */
  public int[] roll = new int[4];
  /**
   * Desired pitch angle in radians +-PI (+-32767)
   */
  public int[] pitch = new int[4];
  /**
   * Desired yaw angle in radians, scaled to int16 +-PI (+-32767)
   */
  public int[] yaw = new int[4];
  /**
   * Collective thrust, scaled to uint16 (0..65535)
   */
  public int[] thrust = new int[4];
  /**
   * ID of the quadrotor group (0 - 255, up to 256 groups supported)
   */
  public int group;
  /**
   * ID of the flight mode (0 - 255, up to 256 modes supported)
   */
  public int mode;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  for (int i=0; i<4; i++) {
    roll[i] = (int)dis.readShort();
  }
  for (int i=0; i<4; i++) {
    pitch[i] = (int)dis.readShort();
  }
  for (int i=0; i<4; i++) {
    yaw[i] = (int)dis.readShort();
  }
  for (int i=0; i<4; i++) {
    thrust[i] = (int)dis.readUnsignedShort()&0x00FFFF;
  }
  group = (int)dis.readUnsignedByte()&0x00FF;
  mode = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+34];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  for (int i=0; i<4; i++) {
    dos.writeShort(roll[i]&0x00FFFF);
  }
  for (int i=0; i<4; i++) {
    dos.writeShort(pitch[i]&0x00FFFF);
  }
  for (int i=0; i<4; i++) {
    dos.writeShort(yaw[i]&0x00FFFF);
  }
  for (int i=0; i<4; i++) {
    dos.writeShort(thrust[i]&0x00FFFF);
  }
  dos.writeByte(group&0x00FF);
  dos.writeByte(mode&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 34);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[40] = crcl;
  buffer[41] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_QUAD_SWARM_ROLL_PITCH_YAW_THRUST : " +   "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  thrust="+thrust+  "  group="+group+  "  mode="+mode;}
}
