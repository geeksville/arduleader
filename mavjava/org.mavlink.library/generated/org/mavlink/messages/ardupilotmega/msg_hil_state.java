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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_usec = (long)dis.readLong();
  roll = (float)dis.readFloat();
  pitch = (float)dis.readFloat();
  yaw = (float)dis.readFloat();
  rollspeed = (float)dis.readFloat();
  pitchspeed = (float)dis.readFloat();
  yawspeed = (float)dis.readFloat();
  lat = (int)dis.readInt();
  lon = (int)dis.readInt();
  alt = (int)dis.readInt();
  vx = (int)dis.readShort();
  vy = (int)dis.readShort();
  vz = (int)dis.readShort();
  xacc = (int)dis.readShort();
  yacc = (int)dis.readShort();
  zacc = (int)dis.readShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+56];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(time_usec);
  dos.writeFloat(roll);
  dos.writeFloat(pitch);
  dos.writeFloat(yaw);
  dos.writeFloat(rollspeed);
  dos.writeFloat(pitchspeed);
  dos.writeFloat(yawspeed);
  dos.writeInt((int)(lat&0x00FFFFFFFF));
  dos.writeInt((int)(lon&0x00FFFFFFFF));
  dos.writeInt((int)(alt&0x00FFFFFFFF));
  dos.writeShort(vx&0x00FFFF);
  dos.writeShort(vy&0x00FFFF);
  dos.writeShort(vz&0x00FFFF);
  dos.writeShort(xacc&0x00FFFF);
  dos.writeShort(yacc&0x00FFFF);
  dos.writeShort(zacc&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
