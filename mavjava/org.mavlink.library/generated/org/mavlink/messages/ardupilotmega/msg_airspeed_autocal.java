/**
 * Generated class : msg_airspeed_autocal
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
 * Class msg_airspeed_autocal
 * Airspeed auto-calibration
 **/
public class msg_airspeed_autocal extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_AIRSPEED_AUTOCAL = 174;
  private static final long serialVersionUID = MAVLINK_MSG_ID_AIRSPEED_AUTOCAL;
  public msg_airspeed_autocal(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_AIRSPEED_AUTOCAL;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 48;
}

  /**
   * GPS velocity north m/s
   */
  public float vx;
  /**
   * GPS velocity east m/s
   */
  public float vy;
  /**
   * GPS velocity down m/s
   */
  public float vz;
  /**
   * Differential pressure pascals
   */
  public float diff_pressure;
  /**
   * Estimated to true airspeed ratio
   */
  public float EAS2TAS;
  /**
   * Airspeed ratio
   */
  public float ratio;
  /**
   * EKF state x
   */
  public float state_x;
  /**
   * EKF state y
   */
  public float state_y;
  /**
   * EKF state z
   */
  public float state_z;
  /**
   * EKF Pax
   */
  public float Pax;
  /**
   * EKF Pby
   */
  public float Pby;
  /**
   * EKF Pcz
   */
  public float Pcz;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  vx = (float)dis.getFloat();
  vy = (float)dis.getFloat();
  vz = (float)dis.getFloat();
  diff_pressure = (float)dis.getFloat();
  EAS2TAS = (float)dis.getFloat();
  ratio = (float)dis.getFloat();
  state_x = (float)dis.getFloat();
  state_y = (float)dis.getFloat();
  state_z = (float)dis.getFloat();
  Pax = (float)dis.getFloat();
  Pby = (float)dis.getFloat();
  Pcz = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+48];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(vx);
  dos.putFloat(vy);
  dos.putFloat(vz);
  dos.putFloat(diff_pressure);
  dos.putFloat(EAS2TAS);
  dos.putFloat(ratio);
  dos.putFloat(state_x);
  dos.putFloat(state_y);
  dos.putFloat(state_z);
  dos.putFloat(Pax);
  dos.putFloat(Pby);
  dos.putFloat(Pcz);
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 48);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[54] = crcl;
  buffer[55] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_AIRSPEED_AUTOCAL : " +   "  vx="+vx+  "  vy="+vy+  "  vz="+vz+  "  diff_pressure="+diff_pressure+  "  EAS2TAS="+EAS2TAS+  "  ratio="+ratio+  "  state_x="+state_x+  "  state_y="+state_y+  "  state_z="+state_z+  "  Pax="+Pax+  "  Pby="+Pby+  "  Pcz="+Pcz;}
}
