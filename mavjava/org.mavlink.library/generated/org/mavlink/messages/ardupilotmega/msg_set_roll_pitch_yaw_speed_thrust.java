/**
 * Generated class : msg_set_roll_pitch_yaw_speed_thrust
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
 * Class msg_set_roll_pitch_yaw_speed_thrust
 * Set roll, pitch and yaw.
 **/
public class msg_set_roll_pitch_yaw_speed_thrust extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_SPEED_THRUST = 57;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_SPEED_THRUST;
  public msg_set_roll_pitch_yaw_speed_thrust(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_SPEED_THRUST;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 18;
}

  /**
   * Desired roll angular speed in rad/s
   */
  public float roll_speed;
  /**
   * Desired pitch angular speed in rad/s
   */
  public float pitch_speed;
  /**
   * Desired yaw angular speed in rad/s
   */
  public float yaw_speed;
  /**
   * Collective thrust, normalized to 0 .. 1
   */
  public float thrust;
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
  roll_speed = (float)dis.readFloat();
  pitch_speed = (float)dis.readFloat();
  yaw_speed = (float)dis.readFloat();
  thrust = (float)dis.readFloat();
  target_system = (int)dis.readUnsignedByte()&0x00FF;
  target_component = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+18];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(roll_speed);
  dos.writeFloat(pitch_speed);
  dos.writeFloat(yaw_speed);
  dos.writeFloat(thrust);
  dos.writeByte(target_system&0x00FF);
  dos.writeByte(target_component&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 18);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[24] = crcl;
  buffer[25] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_ROLL_PITCH_YAW_SPEED_THRUST : " +   "  roll_speed="+roll_speed+  "  pitch_speed="+pitch_speed+  "  yaw_speed="+yaw_speed+  "  thrust="+thrust+  "  target_system="+target_system+  "  target_component="+target_component;}
}
