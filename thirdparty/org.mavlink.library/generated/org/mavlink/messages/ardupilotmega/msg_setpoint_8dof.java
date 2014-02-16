/**
 * Generated class : msg_setpoint_8dof
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
 * Class msg_setpoint_8dof
 * Set the 8 DOF setpoint for a controller.
 **/
public class msg_setpoint_8dof extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SETPOINT_8DOF = 148;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SETPOINT_8DOF;
  public msg_setpoint_8dof(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SETPOINT_8DOF;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 33;
}

  /**
   * Value 1
   */
  public float val1;
  /**
   * Value 2
   */
  public float val2;
  /**
   * Value 3
   */
  public float val3;
  /**
   * Value 4
   */
  public float val4;
  /**
   * Value 5
   */
  public float val5;
  /**
   * Value 6
   */
  public float val6;
  /**
   * Value 7
   */
  public float val7;
  /**
   * Value 8
   */
  public float val8;
  /**
   * System ID
   */
  public int target_system;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  val1 = (float)dis.getFloat();
  val2 = (float)dis.getFloat();
  val3 = (float)dis.getFloat();
  val4 = (float)dis.getFloat();
  val5 = (float)dis.getFloat();
  val6 = (float)dis.getFloat();
  val7 = (float)dis.getFloat();
  val8 = (float)dis.getFloat();
  target_system = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+33];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(val1);
  dos.putFloat(val2);
  dos.putFloat(val3);
  dos.putFloat(val4);
  dos.putFloat(val5);
  dos.putFloat(val6);
  dos.putFloat(val7);
  dos.putFloat(val8);
  dos.put((byte)(target_system&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 33);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[39] = crcl;
  buffer[40] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SETPOINT_8DOF : " +   "  val1="+val1+  "  val2="+val2+  "  val3="+val3+  "  val4="+val4+  "  val5="+val5+  "  val6="+val6+  "  val7="+val7+  "  val8="+val8+  "  target_system="+target_system;}
}
