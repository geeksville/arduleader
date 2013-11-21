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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public void decode(ByteBuffer dis) throws IOException {
  trans_x = (float)dis.getFloat();
  trans_y = (float)dis.getFloat();
  trans_z = (float)dis.getFloat();
  rot_x = (float)dis.getFloat();
  rot_y = (float)dis.getFloat();
  rot_z = (float)dis.getFloat();
  target_system = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+25];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(trans_x);
  dos.putFloat(trans_y);
  dos.putFloat(trans_z);
  dos.putFloat(rot_x);
  dos.putFloat(rot_y);
  dos.putFloat(rot_z);
  dos.put((byte)(target_system&0x00FF));
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
