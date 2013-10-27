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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public void decode(ByteBuffer dis) throws IOException {
  motor_front_nw = (int)dis.getShort()&0x00FFFF;
  motor_right_ne = (int)dis.getShort()&0x00FFFF;
  motor_back_se = (int)dis.getShort()&0x00FFFF;
  motor_left_sw = (int)dis.getShort()&0x00FFFF;
  target_system = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+9];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(motor_front_nw&0x00FFFF));
  dos.putShort((short)(motor_right_ne&0x00FFFF));
  dos.putShort((short)(motor_back_se&0x00FFFF));
  dos.putShort((short)(motor_left_sw&0x00FFFF));
  dos.put((byte)(target_system&0x00FF));
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
