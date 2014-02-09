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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public void decode(ByteBuffer dis) throws IOException {
  q1 = (float)dis.getFloat();
  q2 = (float)dis.getFloat();
  q3 = (float)dis.getFloat();
  q4 = (float)dis.getFloat();
  roll = (float)dis.getFloat();
  pitch = (float)dis.getFloat();
  yaw = (float)dis.getFloat();
  xacc = (float)dis.getFloat();
  yacc = (float)dis.getFloat();
  zacc = (float)dis.getFloat();
  xgyro = (float)dis.getFloat();
  ygyro = (float)dis.getFloat();
  zgyro = (float)dis.getFloat();
  lat = (float)dis.getFloat();
  lon = (float)dis.getFloat();
  alt = (float)dis.getFloat();
  std_dev_horz = (float)dis.getFloat();
  std_dev_vert = (float)dis.getFloat();
  vn = (float)dis.getFloat();
  ve = (float)dis.getFloat();
  vd = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+84];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(q1);
  dos.putFloat(q2);
  dos.putFloat(q3);
  dos.putFloat(q4);
  dos.putFloat(roll);
  dos.putFloat(pitch);
  dos.putFloat(yaw);
  dos.putFloat(xacc);
  dos.putFloat(yacc);
  dos.putFloat(zacc);
  dos.putFloat(xgyro);
  dos.putFloat(ygyro);
  dos.putFloat(zgyro);
  dos.putFloat(lat);
  dos.putFloat(lon);
  dos.putFloat(alt);
  dos.putFloat(std_dev_horz);
  dos.putFloat(std_dev_vert);
  dos.putFloat(vn);
  dos.putFloat(ve);
  dos.putFloat(vd);
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
