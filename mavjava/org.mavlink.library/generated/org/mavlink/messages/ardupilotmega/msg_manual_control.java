/**
 * Generated class : msg_manual_control
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
 * Class msg_manual_control
 * 
 **/
public class msg_manual_control extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MANUAL_CONTROL = 69;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MANUAL_CONTROL;
  public msg_manual_control(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MANUAL_CONTROL;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 21;
}

  /**
   * roll
   */
  public float roll;
  /**
   * pitch
   */
  public float pitch;
  /**
   * yaw
   */
  public float yaw;
  /**
   * thrust
   */
  public float thrust;
  /**
   * The system to be controlled
   */
  public int target;
  /**
   * roll control enabled auto:0, manual:1
   */
  public int roll_manual;
  /**
   * pitch auto:0, manual:1
   */
  public int pitch_manual;
  /**
   * yaw auto:0, manual:1
   */
  public int yaw_manual;
  /**
   * thrust auto:0, manual:1
   */
  public int thrust_manual;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  roll = (float)dis.readFloat();
  pitch = (float)dis.readFloat();
  yaw = (float)dis.readFloat();
  thrust = (float)dis.readFloat();
  target = (int)dis.readUnsignedByte()&0x00FF;
  roll_manual = (int)dis.readUnsignedByte()&0x00FF;
  pitch_manual = (int)dis.readUnsignedByte()&0x00FF;
  yaw_manual = (int)dis.readUnsignedByte()&0x00FF;
  thrust_manual = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+21];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(roll);
  dos.writeFloat(pitch);
  dos.writeFloat(yaw);
  dos.writeFloat(thrust);
  dos.writeByte(target&0x00FF);
  dos.writeByte(roll_manual&0x00FF);
  dos.writeByte(pitch_manual&0x00FF);
  dos.writeByte(yaw_manual&0x00FF);
  dos.writeByte(thrust_manual&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 21);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[27] = crcl;
  buffer[28] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MANUAL_CONTROL : " +   "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  thrust="+thrust+  "  target="+target+  "  roll_manual="+roll_manual+  "  pitch_manual="+pitch_manual+  "  yaw_manual="+yaw_manual+  "  thrust_manual="+thrust_manual;}
}
