/**
 * Generated class : msg_setpoint_6dof
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
 * Class msg_setpoint_6dof
 * Set the 6 DOF setpoint for a attitude and position controller.
 **/
public class msg_setpoint_6dof extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SETPOINT_6DOF = 149;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SETPOINT_6DOF;
  public msg_setpoint_6dof(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SETPOINT_6DOF;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 25;
}

  /**
   * Translational Component in x
   */
  public float trans_x;
  /**
   * Translational Component in y
   */
  public float trans_y;
  /**
   * Translational Component in z
   */
  public float trans_z;
  /**
   * Rotational Component in x
   */
  public float rot_x;
  /**
   * Rotational Component in y
   */
  public float rot_y;
  /**
   * Rotational Component in z
   */
  public float rot_z;
  /**
   * System ID
   */
  public int target_system;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  trans_x = (float)dis.readFloat();
  trans_y = (float)dis.readFloat();
  trans_z = (float)dis.readFloat();
  rot_x = (float)dis.readFloat();
  rot_y = (float)dis.readFloat();
  rot_z = (float)dis.readFloat();
  target_system = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+25];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(trans_x);
  dos.writeFloat(trans_y);
  dos.writeFloat(trans_z);
  dos.writeFloat(rot_x);
  dos.writeFloat(rot_y);
  dos.writeFloat(rot_z);
  dos.writeByte(target_system&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 25);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[31] = crcl;
  buffer[32] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SETPOINT_6DOF : " +   "  trans_x="+trans_x+  "  trans_y="+trans_y+  "  trans_z="+trans_z+  "  rot_x="+rot_x+  "  rot_y="+rot_y+  "  rot_z="+rot_z+  "  target_system="+target_system;}
}
