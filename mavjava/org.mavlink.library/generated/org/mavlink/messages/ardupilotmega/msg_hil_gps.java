/**
 * Generated class : msg_hil_gps
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
 * Class msg_hil_gps
 * The global position, as returned by the Global Positioning System (GPS). This is
                 NOT the global position estimate of the sytem, but rather a RAW sensor value. See message GLOBAL_POSITION for the global position estimate. Coordinate frame is right-handed, Z-axis up (GPS frame).
 **/
public class msg_hil_gps extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_HIL_GPS = 113;
  private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_GPS;
  public msg_hil_gps(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_HIL_GPS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 36;
}

  /**
   * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
   */
  public long time_usec;
  /**
   * Latitude (WGS84), in degrees * 1E7
   */
  public long lat;
  /**
   * Longitude (WGS84), in degrees * 1E7
   */
  public long lon;
  /**
   * Altitude (WGS84), in meters * 1000 (positive for up)
   */
  public long alt;
  /**
   * GPS HDOP horizontal dilution of position in cm (m*100). If unknown, set to: 65535
   */
  public int eph;
  /**
   * GPS VDOP horizontal dilution of position in cm (m*100). If unknown, set to: 65535
   */
  public int epv;
  /**
   * GPS ground speed (m/s * 100). If unknown, set to: 65535
   */
  public int vel;
  /**
   * GPS velocity in cm/s in NORTH direction in earth-fixed NED frame
   */
  public int vn;
  /**
   * GPS velocity in cm/s in EAST direction in earth-fixed NED frame
   */
  public int ve;
  /**
   * GPS velocity in cm/s in DOWN direction in earth-fixed NED frame
   */
  public int vd;
  /**
   * Course over ground (NOT heading, but direction of movement) in degrees * 100, 0.0..359.99 degrees. If unknown, set to: 65535
   */
  public int cog;
  /**
   * 0-1: no fix, 2: 2D fix, 3: 3D fix. Some applications will not use the value of this field unless it is at least two, so always correctly fill in the fix.
   */
  public int fix_type;
  /**
   * Number of satellites visible. If unknown, set to 255
   */
  public int satellites_visible;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_usec = (long)dis.readLong();
  lat = (int)dis.readInt();
  lon = (int)dis.readInt();
  alt = (int)dis.readInt();
  eph = (int)dis.readUnsignedShort()&0x00FFFF;
  epv = (int)dis.readUnsignedShort()&0x00FFFF;
  vel = (int)dis.readUnsignedShort()&0x00FFFF;
  vn = (int)dis.readShort();
  ve = (int)dis.readShort();
  vd = (int)dis.readShort();
  cog = (int)dis.readUnsignedShort()&0x00FFFF;
  fix_type = (int)dis.readUnsignedByte()&0x00FF;
  satellites_visible = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+36];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(time_usec);
  dos.writeInt((int)(lat&0x00FFFFFFFF));
  dos.writeInt((int)(lon&0x00FFFFFFFF));
  dos.writeInt((int)(alt&0x00FFFFFFFF));
  dos.writeShort(eph&0x00FFFF);
  dos.writeShort(epv&0x00FFFF);
  dos.writeShort(vel&0x00FFFF);
  dos.writeShort(vn&0x00FFFF);
  dos.writeShort(ve&0x00FFFF);
  dos.writeShort(vd&0x00FFFF);
  dos.writeShort(cog&0x00FFFF);
  dos.writeByte(fix_type&0x00FF);
  dos.writeByte(satellites_visible&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 36);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[42] = crcl;
  buffer[43] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_HIL_GPS : " +   "  time_usec="+time_usec+  "  lat="+lat+  "  lon="+lon+  "  alt="+alt+  "  eph="+eph+  "  epv="+epv+  "  vel="+vel+  "  vn="+vn+  "  ve="+ve+  "  vd="+vd+  "  cog="+cog+  "  fix_type="+fix_type+  "  satellites_visible="+satellites_visible;}
}
