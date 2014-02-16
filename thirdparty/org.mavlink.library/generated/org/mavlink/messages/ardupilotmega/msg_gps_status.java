/**
 * Generated class : msg_gps_status
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
 * Class msg_gps_status
 * The positioning status, as reported by GPS. This message is intended to display status information about each satellite visible to the receiver. See message GLOBAL_POSITION for the global position estimate. This message can contain information for up to 20 satellites.
 **/
public class msg_gps_status extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_GPS_STATUS = 25;
  private static final long serialVersionUID = MAVLINK_MSG_ID_GPS_STATUS;
  public msg_gps_status(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_GPS_STATUS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 101;
}

  /**
   * Number of satellites visible
   */
  public int satellites_visible;
  /**
   * Global satellite ID
   */
  public int[] satellite_prn = new int[20];
  /**
   * 0: Satellite not used, 1: used for localization
   */
  public int[] satellite_used = new int[20];
  /**
   * Elevation (0: right on top of receiver, 90: on the horizon) of satellite
   */
  public int[] satellite_elevation = new int[20];
  /**
   * Direction of satellite, 0: 0 deg, 255: 360 deg.
   */
  public int[] satellite_azimuth = new int[20];
  /**
   * Signal to noise ratio of satellite
   */
  public int[] satellite_snr = new int[20];
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  satellites_visible = (int)dis.get()&0x00FF;
  for (int i=0; i<20; i++) {
    satellite_prn[i] = (int)dis.get()&0x00FF;
  }
  for (int i=0; i<20; i++) {
    satellite_used[i] = (int)dis.get()&0x00FF;
  }
  for (int i=0; i<20; i++) {
    satellite_elevation[i] = (int)dis.get()&0x00FF;
  }
  for (int i=0; i<20; i++) {
    satellite_azimuth[i] = (int)dis.get()&0x00FF;
  }
  for (int i=0; i<20; i++) {
    satellite_snr[i] = (int)dis.get()&0x00FF;
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+101];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.put((byte)(satellites_visible&0x00FF));
  for (int i=0; i<20; i++) {
    dos.put((byte)(satellite_prn[i]&0x00FF));
  }
  for (int i=0; i<20; i++) {
    dos.put((byte)(satellite_used[i]&0x00FF));
  }
  for (int i=0; i<20; i++) {
    dos.put((byte)(satellite_elevation[i]&0x00FF));
  }
  for (int i=0; i<20; i++) {
    dos.put((byte)(satellite_azimuth[i]&0x00FF));
  }
  for (int i=0; i<20; i++) {
    dos.put((byte)(satellite_snr[i]&0x00FF));
  }
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 101);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[107] = crcl;
  buffer[108] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_GPS_STATUS : " +   "  satellites_visible="+satellites_visible+  "  satellite_prn="+satellite_prn+  "  satellite_used="+satellite_used+  "  satellite_elevation="+satellite_elevation+  "  satellite_azimuth="+satellite_azimuth+  "  satellite_snr="+satellite_snr;}
}
