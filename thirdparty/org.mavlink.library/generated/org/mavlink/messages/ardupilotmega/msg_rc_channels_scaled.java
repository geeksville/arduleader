/**
 * Generated class : msg_rc_channels_scaled
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
 * Class msg_rc_channels_scaled
 * The scaled values of the RC channels received. (-100%) -10000, (0%) 0, (100%) 10000. Channels that are inactive should be set to UINT16_MAX.
 **/
public class msg_rc_channels_scaled extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_RC_CHANNELS_SCALED = 34;
  private static final long serialVersionUID = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
  public msg_rc_channels_scaled(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_RC_CHANNELS_SCALED;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 22;
}

  /**
   * Timestamp (milliseconds since system boot)
   */
  public long time_boot_ms;
  /**
   * RC channel 1 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
   */
  public int chan1_scaled;
  /**
   * RC channel 2 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
   */
  public int chan2_scaled;
  /**
   * RC channel 3 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
   */
  public int chan3_scaled;
  /**
   * RC channel 4 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
   */
  public int chan4_scaled;
  /**
   * RC channel 5 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
   */
  public int chan5_scaled;
  /**
   * RC channel 6 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
   */
  public int chan6_scaled;
  /**
   * RC channel 7 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
   */
  public int chan7_scaled;
  /**
   * RC channel 8 value scaled, (-100%) -10000, (0%) 0, (100%) 10000, (invalid) INT16_MAX.
   */
  public int chan8_scaled;
  /**
   * Servo output port (set of 8 outputs = 1 port). Most MAVs will just use one, but this allows for more than 8 servos.
   */
  public int port;
  /**
   * Receive signal strength indicator, 0: 0%, 100: 100%, 255: invalid/unknown.
   */
  public int rssi;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  chan1_scaled = (int)dis.getShort();
  chan2_scaled = (int)dis.getShort();
  chan3_scaled = (int)dis.getShort();
  chan4_scaled = (int)dis.getShort();
  chan5_scaled = (int)dis.getShort();
  chan6_scaled = (int)dis.getShort();
  chan7_scaled = (int)dis.getShort();
  chan8_scaled = (int)dis.getShort();
  port = (int)dis.get()&0x00FF;
  rssi = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+22];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.putShort((short)(chan1_scaled&0x00FFFF));
  dos.putShort((short)(chan2_scaled&0x00FFFF));
  dos.putShort((short)(chan3_scaled&0x00FFFF));
  dos.putShort((short)(chan4_scaled&0x00FFFF));
  dos.putShort((short)(chan5_scaled&0x00FFFF));
  dos.putShort((short)(chan6_scaled&0x00FFFF));
  dos.putShort((short)(chan7_scaled&0x00FFFF));
  dos.putShort((short)(chan8_scaled&0x00FFFF));
  dos.put((byte)(port&0x00FF));
  dos.put((byte)(rssi&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 22);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[28] = crcl;
  buffer[29] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_RC_CHANNELS_SCALED : " +   "  time_boot_ms="+time_boot_ms+  "  chan1_scaled="+chan1_scaled+  "  chan2_scaled="+chan2_scaled+  "  chan3_scaled="+chan3_scaled+  "  chan4_scaled="+chan4_scaled+  "  chan5_scaled="+chan5_scaled+  "  chan6_scaled="+chan6_scaled+  "  chan7_scaled="+chan7_scaled+  "  chan8_scaled="+chan8_scaled+  "  port="+port+  "  rssi="+rssi;}
}
