/**
 * Generated class : msg_vfr_hud
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
 * Class msg_vfr_hud
 * Metrics typically displayed on a HUD for fixed wing aircraft
 **/
public class msg_vfr_hud extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_VFR_HUD = 74;
  private static final long serialVersionUID = MAVLINK_MSG_ID_VFR_HUD;
  public msg_vfr_hud(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_VFR_HUD;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 20;
}

  /**
   * Current airspeed in m/s
   */
  public float airspeed;
  /**
   * Current ground speed in m/s
   */
  public float groundspeed;
  /**
   * Current altitude (MSL), in meters
   */
  public float alt;
  /**
   * Current climb rate in meters/second
   */
  public float climb;
  /**
   * Current heading in degrees, in compass units (0..360, 0=north)
   */
  public int heading;
  /**
   * Current throttle setting in integer percent, 0 to 100
   */
  public int throttle;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  airspeed = (float)dis.readFloat();
  groundspeed = (float)dis.readFloat();
  alt = (float)dis.readFloat();
  climb = (float)dis.readFloat();
  heading = (int)dis.readShort();
  throttle = (int)dis.readUnsignedShort()&0x00FFFF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+20];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(airspeed);
  dos.writeFloat(groundspeed);
  dos.writeFloat(alt);
  dos.writeFloat(climb);
  dos.writeShort(heading&0x00FFFF);
  dos.writeShort(throttle&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 20);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[26] = crcl;
  buffer[27] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_VFR_HUD : " +   "  airspeed="+airspeed+  "  groundspeed="+groundspeed+  "  alt="+alt+  "  climb="+climb+  "  heading="+heading+  "  throttle="+throttle;}
}
