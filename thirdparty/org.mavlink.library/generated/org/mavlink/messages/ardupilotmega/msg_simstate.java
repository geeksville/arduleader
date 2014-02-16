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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
   * Latitude in degrees * 1E7
   */
  public long lat;
  /**
   * Longitude in degrees * 1E7
   */
  public long lng;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  roll = (float)dis.getFloat();
  pitch = (float)dis.getFloat();
  yaw = (float)dis.getFloat();
  xacc = (float)dis.getFloat();
  yacc = (float)dis.getFloat();
  zacc = (float)dis.getFloat();
  xgyro = (float)dis.getFloat();
  ygyro = (float)dis.getFloat();
  zgyro = (float)dis.getFloat();
  lat = (int)dis.getInt();
  lng = (int)dis.getInt();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+44];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(roll);
  dos.putFloat(pitch);
  dos.putFloat(yaw);
  dos.putFloat(xacc);
  dos.putFloat(yacc);
  dos.putFloat(zacc);
  dos.putFloat(xgyro);
  dos.putFloat(ygyro);
  dos.putFloat(zgyro);
  dos.putInt((int)(lat&0x00FFFFFFFF));
  dos.putInt((int)(lng&0x00FFFFFFFF));
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
