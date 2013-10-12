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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  vx = (float)dis.readFloat();
  vy = (float)dis.readFloat();
  vz = (float)dis.readFloat();
  diff_pressure = (float)dis.readFloat();
  EAS2TAS = (float)dis.readFloat();
  ratio = (float)dis.readFloat();
  state_x = (float)dis.readFloat();
  state_y = (float)dis.readFloat();
  state_z = (float)dis.readFloat();
  Pax = (float)dis.readFloat();
  Pby = (float)dis.readFloat();
  Pcz = (float)dis.readFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+48];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(vx);
  dos.writeFloat(vy);
  dos.writeFloat(vz);
  dos.writeFloat(diff_pressure);
  dos.writeFloat(EAS2TAS);
  dos.writeFloat(ratio);
  dos.writeFloat(state_x);
  dos.writeFloat(state_y);
  dos.writeFloat(state_z);
  dos.writeFloat(Pax);
  dos.writeFloat(Pby);
  dos.writeFloat(Pcz);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
