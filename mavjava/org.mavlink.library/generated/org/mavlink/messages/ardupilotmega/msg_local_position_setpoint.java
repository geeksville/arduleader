/**
 * Generated class : msg_local_position_setpoint
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
 * Class msg_local_position_setpoint
 * Transmit the current local setpoint of the controller to other MAVs (collision avoidance) and to the GCS.
 **/
public class msg_local_position_setpoint extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT = 51;
  private static final long serialVersionUID = MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT;
  public msg_local_position_setpoint(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 17;
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
  coordinate_frame = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+17];
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
  dos.put((byte)(coordinate_frame&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 17);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[23] = crcl;
  buffer[24] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT : " +   "  x="+x+  "  y="+y+  "  z="+z+  "  yaw="+yaw+  "  coordinate_frame="+coordinate_frame;}
}
