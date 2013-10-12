/**
 * Generated class : msg_set_quad_swarm_led_roll_pitch_yaw_thrust
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
 * Class msg_set_quad_swarm_led_roll_pitch_yaw_thrust
 * Setpoint for up to four quadrotors in a group / wing
 **/
public class msg_set_quad_swarm_led_roll_pitch_yaw_thrust extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_QUAD_SWARM_LED_ROLL_PITCH_YAW_THRUST = 63;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_QUAD_SWARM_LED_ROLL_PITCH_YAW_THRUST;
  public msg_set_quad_swarm_led_roll_pitch_yaw_thrust(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_QUAD_SWARM_LED_ROLL_PITCH_YAW_THRUST;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 46;
}

  /**
   * Desired roll angle in radians +-PI (+-INT16_MAX)
   */
  public int[] roll = new int[4];
  /**
   * Desired pitch angle in radians +-PI (+-INT16_MAX)
   */
  public int[] pitch = new int[4];
  /**
   * Desired yaw angle in radians, scaled to int16 +-PI (+-INT16_MAX)
   */
  public int[] yaw = new int[4];
  /**
   * Collective thrust, scaled to uint16 (0..UINT16_MAX)
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
   * RGB red channel (0-255)
   */
  public int[] led_red = new int[4];
  /**
   * RGB green channel (0-255)
   */
  public int[] led_blue = new int[4];
  /**
   * RGB blue channel (0-255)
   */
  public int[] led_green = new int[4];
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
  for (int i=0; i<4; i++) {
    led_red[i] = (int)dis.readUnsignedByte()&0x00FF;
  }
  for (int i=0; i<4; i++) {
    led_blue[i] = (int)dis.readUnsignedByte()&0x00FF;
  }
  for (int i=0; i<4; i++) {
    led_green[i] = (int)dis.readUnsignedByte()&0x00FF;
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+46];
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
  for (int i=0; i<4; i++) {
    dos.writeByte(led_red[i]&0x00FF);
  }
  for (int i=0; i<4; i++) {
    dos.writeByte(led_blue[i]&0x00FF);
  }
  for (int i=0; i<4; i++) {
    dos.writeByte(led_green[i]&0x00FF);
  }
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 46);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[52] = crcl;
  buffer[53] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_QUAD_SWARM_LED_ROLL_PITCH_YAW_THRUST : " +   "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  thrust="+thrust+  "  group="+group+  "  mode="+mode+  "  led_red="+led_red+  "  led_blue="+led_blue+  "  led_green="+led_green;}
}
