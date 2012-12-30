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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  x = (float)dis.readFloat();
  y = (float)dis.readFloat();
  z = (float)dis.readFloat();
  yaw = (float)dis.readFloat();
  coordinate_frame = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+17];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(x);
  dos.writeFloat(y);
  dos.writeFloat(z);
  dos.writeFloat(yaw);
  dos.writeByte(coordinate_frame&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
