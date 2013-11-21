/**
 * Generated class : msg_hil_state
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
 * Class msg_hil_state
 * DEPRECATED PACKET! Suffers from missing airspeed fields and singularities due to Euler angles. Please use HIL_STATE_QUATERNION instead. Sent from simulation to autopilot. This packet is useful for high throughput applications such as hardware in the loop simulations.
 **/
public class msg_hil_state extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_HIL_STATE = 90;
  private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_STATE;
  public msg_hil_state(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_HIL_STATE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 56;
}

  /**
   * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
   */
  public long time_usec;
  /**
   * Roll angle (rad)
   */
  public float roll;
  /**
   * Pitch angle (rad)
   */
  public float pitch;
  /**
   * Yaw angle (rad)
   */
  public float yaw;
  /**
   * Body frame roll / phi angular speed (rad/s)
   */
  public float rollspeed;
  /**
   * Body frame pitch / theta angular speed (rad/s)
   */
  public float pitchspeed;
  /**
   * Body frame yaw / psi angular speed (rad/s)
   */
  public float yawspeed;
  /**
   * Latitude, expressed as * 1E7
   */
  public long lat;
  /**
   * Longitude, expressed as * 1E7
   */
  public long lon;
  /**
   * Altitude in meters, expressed as * 1000 (millimeters)
   */
  public long alt;
  /**
   * Ground X Speed (Latitude), expressed as m/s * 100
   */
  public int vx;
  /**
   * Ground Y Speed (Longitude), expressed as m/s * 100
   */
  public int vy;
  /**
   * Ground Z Speed (Altitude), expressed as m/s * 100
   */
  public int vz;
  /**
   * X acceleration (mg)
   */
  public int xacc;
  /**
   * Y acceleration (mg)
   */
  public int yacc;
  /**
   * Z acceleration (mg)
   */
  public int zacc;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (long)dis.getLong();
  roll = (float)dis.getFloat();
  pitch = (float)dis.getFloat();
  yaw = (float)dis.getFloat();
  rollspeed = (float)dis.getFloat();
  pitchspeed = (float)dis.getFloat();
  yawspeed = (float)dis.getFloat();
  lat = (int)dis.getInt();
  lon = (int)dis.getInt();
  alt = (int)dis.getInt();
  vx = (int)dis.getShort();
  vy = (int)dis.getShort();
  vz = (int)dis.getShort();
  xacc = (int)dis.getShort();
  yacc = (int)dis.getShort();
  zacc = (int)dis.getShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+56];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_usec);
  dos.putFloat(roll);
  dos.putFloat(pitch);
  dos.putFloat(yaw);
  dos.putFloat(rollspeed);
  dos.putFloat(pitchspeed);
  dos.putFloat(yawspeed);
  dos.putInt((int)(lat&0x00FFFFFFFF));
  dos.putInt((int)(lon&0x00FFFFFFFF));
  dos.putInt((int)(alt&0x00FFFFFFFF));
  dos.putShort((short)(vx&0x00FFFF));
  dos.putShort((short)(vy&0x00FFFF));
  dos.putShort((short)(vz&0x00FFFF));
  dos.putShort((short)(xacc&0x00FFFF));
  dos.putShort((short)(yacc&0x00FFFF));
  dos.putShort((short)(zacc&0x00FFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 56);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[62] = crcl;
  buffer[63] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_HIL_STATE : " +   "  time_usec="+time_usec+  "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  rollspeed="+rollspeed+  "  pitchspeed="+pitchspeed+  "  yawspeed="+yawspeed+  "  lat="+lat+  "  lon="+lon+  "  alt="+alt+  "  vx="+vx+  "  vy="+vy+  "  vz="+vz+  "  xacc="+xacc+  "  yacc="+yacc+  "  zacc="+zacc;}
}
