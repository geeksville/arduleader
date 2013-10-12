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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  x = (int)dis.readShort();
  y = (int)dis.readShort();
  z = (int)dis.readShort();
  r = (int)dis.readShort();
  buttons = (int)dis.readUnsignedShort()&0x00FFFF;
  target = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+11];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeShort(x&0x00FFFF);
  dos.writeShort(y&0x00FFFF);
  dos.writeShort(z&0x00FFFF);
  dos.writeShort(r&0x00FFFF);
  dos.writeShort(buttons&0x00FFFF);
  dos.writeByte(target&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
