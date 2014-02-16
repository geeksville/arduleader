/**
 * Generated class : msg_set_local_position_setpoint
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
 * Class msg_set_local_position_setpoint
 * Set the setpoint for a local position controller. This is the position in local coordinates the MAV should fly to. This message is sent by the path/MISSION planner to the onboard position controller. As some MAVs have a degree of freedom in yaw (e.g. all helicopters/quadrotors), the desired yaw angle is part of the message.
 **/
public class msg_set_local_position_setpoint extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT = 50;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT;
  public msg_set_local_position_setpoint(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 19;
}

  /**
   * x position
   */
  public float x;
  /**
   * y position
   */
  public float y;
  /**
   * z position
   */
  public float z;
  /**
   * Desired yaw angle
   */
  public float yaw;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
  /**
   * Coordinate frame - valid values are only MAV_FRAME_LOCAL_NED or MAV_FRAME_LOCAL_ENU
   */
  public int coordinate_frame;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  x = (float)dis.getFloat();
  y = (float)dis.getFloat();
  z = (float)dis.getFloat();
  yaw = (float)dis.getFloat();
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  coordinate_frame = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+19];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(x);
  dos.putFloat(y);
  dos.putFloat(z);
  dos.putFloat(yaw);
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  dos.put((byte)(coordinate_frame&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 19);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[25] = crcl;
  buffer[26] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SET_LOCAL_POSITION_SETPOINT : " +   "  x="+x+  "  y="+y+  "  z="+z+  "  yaw="+yaw+  "  target_system="+target_system+  "  target_component="+target_component+  "  coordinate_frame="+coordinate_frame;}
}
