/**
 * Generated class : msg_simstate
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
 * Class msg_simstate
 * Status of simulation environment, if used
 **/
public class msg_simstate extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SIMSTATE = 164;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SIMSTATE;
  public msg_simstate(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SIMSTATE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 44;
}

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
  public float lng;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
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
  lng = (float)dis.readFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+44];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
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
  dos.writeFloat(lng);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 44);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[50] = crcl;
  buffer[51] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SIMSTATE : " +   "  roll="+roll+  "  pitch="+pitch+  "  yaw="+yaw+  "  xacc="+xacc+  "  yacc="+yacc+  "  zacc="+zacc+  "  xgyro="+xgyro+  "  ygyro="+ygyro+  "  zgyro="+zgyro+  "  lat="+lat+  "  lng="+lng;}
}
