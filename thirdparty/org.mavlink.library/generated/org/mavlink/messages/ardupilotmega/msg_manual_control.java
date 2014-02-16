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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
/**
 * Class msg_manual_control
 * This message provides an API for manually controlling the vehicle using standard joystick axes nomenclature, along with a joystick-like input device. Unused axes can be disabled an buttons are also transmit as boolean values of their
 **/
public class msg_manual_control extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MANUAL_CONTROL = 69;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MANUAL_CONTROL;
  public msg_manual_control(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MANUAL_CONTROL;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 11;
}

  /**
   * X-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to forward(1000)-backward(-1000) movement on a joystick and the pitch of a vehicle.
   */
  public int x;
  /**
   * Y-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to left(-1000)-right(1000) movement on a joystick and the roll of a vehicle.
   */
  public int y;
  /**
   * Z-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to a separate slider movement with maximum being 1000 and minimum being -1000 on a joystick and the thrust of a vehicle.
   */
  public int z;
  /**
   * R-axis, normalized to the range [-1000,1000]. A value of INT16_MAX indicates that this axis is invalid. Generally corresponds to a twisting of the joystick, with counter-clockwise being 1000 and clockwise being -1000, and the yaw of a vehicle.
   */
  public int r;
  /**
   * A bitfield corresponding to the joystick buttons' current state, 1 for pressed, 0 for released. The lowest bit corresponds to Button 1.
   */
  public int buttons;
  /**
   * The system to be controlled.
   */
  public int target;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  x = (int)dis.getShort();
  y = (int)dis.getShort();
  z = (int)dis.getShort();
  r = (int)dis.getShort();
  buttons = (int)dis.getShort()&0x00FFFF;
  target = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+11];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(x&0x00FFFF));
  dos.putShort((short)(y&0x00FFFF));
  dos.putShort((short)(z&0x00FFFF));
  dos.putShort((short)(r&0x00FFFF));
  dos.putShort((short)(buttons&0x00FFFF));
  dos.put((byte)(target&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 11);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[17] = crcl;
  buffer[18] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MANUAL_CONTROL : " +   "  x="+x+  "  y="+y+  "  z="+z+  "  r="+r+  "  buttons="+buttons+  "  target="+target;}
}
