/**
 * Generated class : msg_global_position_int
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
 * Class msg_global_position_int
 * The filtered global position (e.g. fused GPS and accelerometers). The position is in GPS-frame (right-handed, Z-up). It
               is designed as scaled integer message since the resolution of float is not sufficient.
 **/
public class msg_global_position_int extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_GLOBAL_POSITION_INT = 33;
  private static final long serialVersionUID = MAVLINK_MSG_ID_GLOBAL_POSITION_INT;
  public msg_global_position_int(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_GLOBAL_POSITION_INT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 28;
}

  /**
   * Timestamp (milliseconds since system boot)
   */
  public long time_boot_ms;
  /**
   * Latitude, expressed as * 1E7
   */
  public long lat;
  /**
   * Longitude, expressed as * 1E7
   */
  public long lon;
  /**
   * Altitude in meters, expressed as * 1000 (millimeters), above MSL
   */
  public long alt;
  /**
   * Altitude above ground in meters, expressed as * 1000 (millimeters)
   */
  public long relative_alt;
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
   * Compass heading in degrees * 100, 0.0..359.99 degrees. If unknown, set to: UINT16_MAX
   */
  public int hdg;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_boot_ms = (int)dis.readInt()&0x00FFFFFFFF;
  lat = (int)dis.readInt();
  lon = (int)dis.readInt();
  alt = (int)dis.readInt();
  relative_alt = (int)dis.readInt();
  vx = (int)dis.readShort();
  vy = (int)dis.readShort();
  vz = (int)dis.readShort();
  hdg = (int)dis.readUnsignedShort()&0x00FFFF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+28];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.writeInt((int)(lat&0x00FFFFFFFF));
  dos.writeInt((int)(lon&0x00FFFFFFFF));
  dos.writeInt((int)(alt&0x00FFFFFFFF));
  dos.writeInt((int)(relative_alt&0x00FFFFFFFF));
  dos.writeShort(vx&0x00FFFF);
  dos.writeShort(vy&0x00FFFF);
  dos.writeShort(vz&0x00FFFF);
  dos.writeShort(hdg&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 28);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[34] = crcl;
  buffer[35] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_GLOBAL_POSITION_INT : " +   "  time_boot_ms="+time_boot_ms+  "  lat="+lat+  "  lon="+lon+  "  alt="+alt+  "  relative_alt="+relative_alt+  "  vx="+vx+  "  vy="+vy+  "  vz="+vz+  "  hdg="+hdg;}
}
