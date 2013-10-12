/**
 * Generated class : msg_sim_state
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
 * Class msg_sim_state
 * Status of simulation environment, if used
 **/
public class msg_sim_state extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SIM_STATE = 108;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SIM_STATE;
  public msg_sim_state(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SIM_STATE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 84;
}

  /**
   * True attitude quaternion component 1
   */
  public float q1;
  /**
   * True attitude quaternion component 2
   */
  public float q2;
  /**
   * True attitude quaternion component 3
   */
  public float q3;
  /**
   * True attitude quaternion component 4
   */
  public float q4;
  /**
   * Attitude roll expressed as Euler angles, not recommended except for human-readable outputs
   */
  public float roll;
  /**
   * Attitude pitch expressed as Euler angles, not recommended except for human-readable outputs
   */
  public float pitch;
  /**
   * Attitude yaw expressed as Euler angles, not recommended except for human-readable outputs
   */
  public float yaw;
  /**
   * X acceleration m/s/s
   */
  public float xacc;
  /**
   * Y acceleration m/s/s
   */
  public float yacc;
  /**
   * Z acceleration m/s/s
   */
  public float zacc;
  /**
   * Angular speed around X axis rad/s
   */
  public float xgyro;
  /**
   * Angular speed around Y axis rad/s
   */
  public float ygyro;
  /**
   * Angular speed around Z axis rad/s
   */
  public float zgyro;
  /**
   * Latitude in degrees
   */
  public float lat;
  /**
   * Longitude in degrees
   */
  public float lon;
  /**
   * Altitude in meters
   */
  public float alt;
  /**
   * Horizontal position standard deviation
   */
  public float std_dev_horz;
  /**
   * Vertical position standard deviation
   */
  public float std_dev_vert;
  /**
   * True velocity in m/s in NORTH direction in earth-fixed NED frame
   */
  public float vn;
  /**
   * True velocity in m/s in EAST direction in earth-fixed NED frame
   */
  public float ve;
  /**
   * True velocity in m/s in DOWN direction in earth-fixed NED frame
   */
  public float vd;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  q1 = (float)dis.readFloat();
  q2 = (float)dis.readFloat();
  q3 = (float)dis.readFloat();
  q4 = (float)dis.readFloat();
  roll = (float)dis.readFloat();
  pitch = (float)dis.readFloat();
  yaw = (float)dis.readFloat();
  xacc = (float)dis.readFloat();
  yacc = (float)dis.readFloat();
  zacc = (float)dis.readFloat();
  xgyro = (float)dis.readFloat();
  ygyro = (float)dis.readFloat();
  zgyro = (float)dis.readFloat();
  lat = (float)dis.readFloat();
  lon = (float)dis.readFloat();
  alt = (float)dis.readFloat();
  std_dev_horz = (float)dis.readFloat();
  std_dev_vert = (float)dis.readFloat();
  vn = (float)dis.readFloat();
  ve = (float)dis.readFloat();
  vd = (float)dis.readFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+84];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(q1);
  dos.writeFloat(q2);
  dos.writeFloat(q3);
  dos.writeFloat(q4);
  dos.writeFloat(roll);
  dos.writeFloat(pitch);
  dos.writeFloat(yaw);
  dos.writeFloat(xacc);
  dos.writeFloat(yacc);
  dos.writeFloat(zacc);
  dos.writeFloat(xgyro);
  dos.writeFloat(ygyro);
  dos.writeFloat(zgyro);
  dos.writeFloat(lat);
  dos.writeFloat(lon);
  dos.writeFloat(alt);
  dos.writeFloat(std_dev_horz);
  dos.writeFloat(std_dev_vert);
  dos.writeFloat(vn);
  dos.writeFloat(ve);
  dos.writeFloat(vd);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 84);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[90] = crcl;
  buffer[91] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SIM_STATE : " +   "  q1="+q1+  "  q2="+q2+  "  q3="+q3+  "  q4="+q4+  "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  xacc="+xacc+  "  yacc="+yacc+  "  zacc="+zacc+  "  xgyro="+xgyro+  "  ygyro="+ygyro+  "  zgyro="+zgyro+  "  lat="+lat+  "  lon="+lon+  "  alt="+alt+  "  std_dev_horz="+std_dev_horz+  "  std_dev_vert="+std_dev_vert+  "  vn="+vn+  "  ve="+ve+  "  vd="+vd;}
}
