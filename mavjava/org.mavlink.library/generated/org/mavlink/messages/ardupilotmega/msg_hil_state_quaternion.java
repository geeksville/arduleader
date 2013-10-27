/**
 * Generated class : msg_hil_state_quaternion
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
 * Class msg_hil_state_quaternion
 * Sent from simulation to autopilot, avoids in contrast to HIL_STATE singularities. This packet is useful for high throughput applications such as hardware in the loop simulations.
 **/
public class msg_hil_state_quaternion extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_HIL_STATE_QUATERNION = 115;
  private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_STATE_QUATERNION;
  public msg_hil_state_quaternion(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_HIL_STATE_QUATERNION;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 64;
}

  /**
   * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
   */
  public long time_usec;
  /**
   * Vehicle attitude expressed as normalized quaternion
   */
  public float[] attitude_quaternion = new float[4];
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
   * Indicated airspeed, expressed as m/s * 100
   */
  public int ind_airspeed;
  /**
   * True airspeed, expressed as m/s * 100
   */
  public int true_airspeed;
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
  for (int i=0; i<4; i++) {
    attitude_quaternion[i] = (float)dis.getFloat();
  }
  rollspeed = (float)dis.getFloat();
  pitchspeed = (float)dis.getFloat();
  yawspeed = (float)dis.getFloat();
  lat = (int)dis.getInt();
  lon = (int)dis.getInt();
  alt = (int)dis.getInt();
  vx = (int)dis.getShort();
  vy = (int)dis.getShort();
  vz = (int)dis.getShort();
  ind_airspeed = (int)dis.getShort()&0x00FFFF;
  true_airspeed = (int)dis.getShort()&0x00FFFF;
  xacc = (int)dis.getShort();
  yacc = (int)dis.getShort();
  zacc = (int)dis.getShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+64];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_usec);
  for (int i=0; i<4; i++) {
    dos.putFloat(attitude_quaternion[i]);
  }
  dos.putFloat(rollspeed);
  dos.putFloat(pitchspeed);
  dos.putFloat(yawspeed);
  dos.putInt((int)(lat&0x00FFFFFFFF));
  dos.putInt((int)(lon&0x00FFFFFFFF));
  dos.putInt((int)(alt&0x00FFFFFFFF));
  dos.putShort((short)(vx&0x00FFFF));
  dos.putShort((short)(vy&0x00FFFF));
  dos.putShort((short)(vz&0x00FFFF));
  dos.putShort((short)(ind_airspeed&0x00FFFF));
  dos.putShort((short)(true_airspeed&0x00FFFF));
  dos.putShort((short)(xacc&0x00FFFF));
  dos.putShort((short)(yacc&0x00FFFF));
  dos.putShort((short)(zacc&0x00FFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 64);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[70] = crcl;
  buffer[71] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_HIL_STATE_QUATERNION : " +   "  time_usec="+time_usec+  "  attitude_quaternion="+attitude_quaternion+  "  rollspeed="+rollspeed+  "  pitchspeed="+pitchspeed+  "  yawspeed="+yawspeed+  "  lat="+lat+  "  lon="+lon+  "  alt="+alt+  "  vx="+vx+  "  vy="+vy+  "  vz="+vz+  "  ind_airspeed="+ind_airspeed+  "  true_airspeed="+true_airspeed+  "  xacc="+xacc+  "  yacc="+yacc+  "  zacc="+zacc;}
}
