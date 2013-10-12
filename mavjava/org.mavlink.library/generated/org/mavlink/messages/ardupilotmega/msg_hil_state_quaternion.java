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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_usec = (long)dis.readLong();
  for (int i=0; i<4; i++) {
    attitude_quaternion[i] = (float)dis.readFloat();
  }
  rollspeed = (float)dis.readFloat();
  pitchspeed = (float)dis.readFloat();
  yawspeed = (float)dis.readFloat();
  lat = (int)dis.readInt();
  lon = (int)dis.readInt();
  alt = (int)dis.readInt();
  vx = (int)dis.readShort();
  vy = (int)dis.readShort();
  vz = (int)dis.readShort();
  ind_airspeed = (int)dis.readUnsignedShort()&0x00FFFF;
  true_airspeed = (int)dis.readUnsignedShort()&0x00FFFF;
  xacc = (int)dis.readShort();
  yacc = (int)dis.readShort();
  zacc = (int)dis.readShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+64];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(time_usec);
  for (int i=0; i<4; i++) {
    dos.writeFloat(attitude_quaternion[i]);
  }
  dos.writeFloat(rollspeed);
  dos.writeFloat(pitchspeed);
  dos.writeFloat(yawspeed);
  dos.writeInt((int)(lat&0x00FFFFFFFF));
  dos.writeInt((int)(lon&0x00FFFFFFFF));
  dos.writeInt((int)(alt&0x00FFFFFFFF));
  dos.writeShort(vx&0x00FFFF);
  dos.writeShort(vy&0x00FFFF);
  dos.writeShort(vz&0x00FFFF);
  dos.writeShort(ind_airspeed&0x00FFFF);
  dos.writeShort(true_airspeed&0x00FFFF);
  dos.writeShort(xacc&0x00FFFF);
  dos.writeShort(yacc&0x00FFFF);
  dos.writeShort(zacc&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
