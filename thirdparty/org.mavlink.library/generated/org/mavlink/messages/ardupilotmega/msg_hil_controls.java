/**
 * Generated class : msg_hil_controls
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
 * Class msg_hil_controls
 * Sent from autopilot to simulation. Hardware in the loop control outputs
 **/
public class msg_hil_controls extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_HIL_CONTROLS = 91;
  private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_CONTROLS;
  public msg_hil_controls(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_HIL_CONTROLS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 42;
}

  /**
   * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
   */
  public long time_usec;
  /**
   * Control output -1 .. 1
   */
  public float roll_ailerons;
  /**
   * Control output -1 .. 1
   */
  public float pitch_elevator;
  /**
   * Control output -1 .. 1
   */
  public float yaw_rudder;
  /**
   * Throttle 0 .. 1
   */
  public float throttle;
  /**
   * Aux 1, -1 .. 1
   */
  public float aux1;
  /**
   * Aux 2, -1 .. 1
   */
  public float aux2;
  /**
   * Aux 3, -1 .. 1
   */
  public float aux3;
  /**
   * Aux 4, -1 .. 1
   */
  public float aux4;
  /**
   * System mode (MAV_MODE)
   */
  public int mode;
  /**
   * Navigation mode (MAV_NAV_MODE)
   */
  public int nav_mode;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (long)dis.getLong();
  roll_ailerons = (float)dis.getFloat();
  pitch_elevator = (float)dis.getFloat();
  yaw_rudder = (float)dis.getFloat();
  throttle = (float)dis.getFloat();
  aux1 = (float)dis.getFloat();
  aux2 = (float)dis.getFloat();
  aux3 = (float)dis.getFloat();
  aux4 = (float)dis.getFloat();
  mode = (int)dis.get()&0x00FF;
  nav_mode = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+42];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_usec);
  dos.putFloat(roll_ailerons);
  dos.putFloat(pitch_elevator);
  dos.putFloat(yaw_rudder);
  dos.putFloat(throttle);
  dos.putFloat(aux1);
  dos.putFloat(aux2);
  dos.putFloat(aux3);
  dos.putFloat(aux4);
  dos.put((byte)(mode&0x00FF));
  dos.put((byte)(nav_mode&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 42);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[48] = crcl;
  buffer[49] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_HIL_CONTROLS : " +   "  time_usec="+time_usec+  "  roll_ailerons="+roll_ailerons+  "  pitch_elevator="+pitch_elevator+  "  yaw_rudder="+yaw_rudder+  "  throttle="+throttle+  "  aux1="+aux1+  "  aux2="+aux2+  "  aux3="+aux3+  "  aux4="+aux4+  "  mode="+mode+  "  nav_mode="+nav_mode;}
}
