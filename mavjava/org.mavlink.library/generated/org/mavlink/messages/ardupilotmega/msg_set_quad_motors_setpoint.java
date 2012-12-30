/**
 * Generated class : msg_set_quad_motors_setpoint
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
 * Class msg_set_quad_motors_setpoint
 * Setpoint in the four motor speeds
 **/
public class msg_set_quad_motors_setpoint extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT = 60;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT;
  public msg_set_quad_motors_setpoint(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 9;
}

  /**
   * Front motor in + configuration, front left motor in x configuration
   */
  public int motor_front_nw;
  /**
   * Right motor in + configuration, front right motor in x configuration
   */
  public int motor_right_ne;
  /**
   * Back motor in + configuration, back right motor in x configuration
   */
  public int motor_back_se;
  /**
   * Left motor in + configuration, back left motor in x configuration
   */
  public int motor_left_sw;
  /**
   * System ID of the system that should set these motor commands
   */
  public int target_system;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  motor_front_nw = (int)dis.readUnsignedShort()&0x00FFFF;
  motor_right_ne = (int)dis.readUnsignedShort()&0x00FFFF;
  motor_back_se = (int)dis.readUnsignedShort()&0x00FFFF;
  motor_left_sw = (int)dis.readUnsignedShort()&0x00FFFF;
  target_system = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+9];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeShort(motor_front_nw&0x00FFFF);
  dos.writeShort(motor_right_ne&0x00FFFF);
  dos.writeShort(motor_back_se&0x00FFFF);
  dos.writeShort(motor_left_sw&0x00FFFF);
  dos.writeByte(target_system&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 9);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[15] = crcl;
  buffer[16] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_QUAD_MOTORS_SETPOINT : " +   "  motor_front_nw="+motor_front_nw+  "  motor_right_ne="+motor_right_ne+  "  motor_back_se="+motor_back_se+  "  motor_left_sw="+motor_left_sw+  "  target_system="+target_system;}
}
