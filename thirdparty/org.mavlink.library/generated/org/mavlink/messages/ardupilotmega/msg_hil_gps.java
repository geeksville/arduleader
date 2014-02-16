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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (long)dis.getLong();
  lat = (int)dis.getInt();
  lon = (int)dis.getInt();
  alt = (int)dis.getInt();
  eph = (int)dis.getShort()&0x00FFFF;
  epv = (int)dis.getShort()&0x00FFFF;
  vel = (int)dis.getShort()&0x00FFFF;
  vn = (int)dis.getShort();
  ve = (int)dis.getShort();
  vd = (int)dis.getShort();
  cog = (int)dis.getShort()&0x00FFFF;
  fix_type = (int)dis.get()&0x00FF;
  satellites_visible = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+36];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_usec);
  dos.putInt((int)(lat&0x00FFFFFFFF));
  dos.putInt((int)(lon&0x00FFFFFFFF));
  dos.putInt((int)(alt&0x00FFFFFFFF));
  dos.putShort((short)(eph&0x00FFFF));
  dos.putShort((short)(epv&0x00FFFF));
  dos.putShort((short)(vel&0x00FFFF));
  dos.putShort((short)(vn&0x00FFFF));
  dos.putShort((short)(ve&0x00FFFF));
  dos.putShort((short)(vd&0x00FFFF));
  dos.putShort((short)(cog&0x00FFFF));
  dos.put((byte)(fix_type&0x00FF));
  dos.put((byte)(satellites_visible&0x00FF));
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
