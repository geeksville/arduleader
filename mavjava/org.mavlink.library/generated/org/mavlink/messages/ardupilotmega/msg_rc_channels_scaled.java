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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
/**
 * Class msg_rc_channels_scaled
 * The scaled values of the RC channels received. (-100%) -10000, (0%) 0, (100%) 10000
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
   * RC channel 1 value scaled, (-100%) -10000, (0%) 0, (100%) 10000
   */
  public int chan1_scaled;
  /**
   * RC channel 2 value scaled, (-100%) -10000, (0%) 0, (100%) 10000
   */
  public int chan2_scaled;
  /**
   * RC channel 3 value scaled, (-100%) -10000, (0%) 0, (100%) 10000
   */
  public int chan3_scaled;
  /**
   * RC channel 4 value scaled, (-100%) -10000, (0%) 0, (100%) 10000
   */
  public int chan4_scaled;
  /**
   * RC channel 5 value scaled, (-100%) -10000, (0%) 0, (100%) 10000
   */
  public int chan5_scaled;
  /**
   * RC channel 6 value scaled, (-100%) -10000, (0%) 0, (100%) 10000
   */
  public int chan6_scaled;
  /**
   * RC channel 7 value scaled, (-100%) -10000, (0%) 0, (100%) 10000
   */
  public int chan7_scaled;
  /**
   * RC channel 8 value scaled, (-100%) -10000, (0%) 0, (100%) 10000
   */
  public int chan8_scaled;
  /**
   * Servo output port (set of 8 outputs = 1 port). Most MAVs will just use one, but this allows to encode more than 8 servos.
   */
  public int port;
  /**
   * Receive signal strength indicator, 0: 0%, 255: 100%
   */
  public int rssi;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_boot_ms = (int)dis.readInt()&0x00FFFFFFFF;
  chan1_scaled = (int)dis.readShort();
  chan2_scaled = (int)dis.readShort();
  chan3_scaled = (int)dis.readShort();
  chan4_scaled = (int)dis.readShort();
  chan5_scaled = (int)dis.readShort();
  chan6_scaled = (int)dis.readShort();
  chan7_scaled = (int)dis.readShort();
  chan8_scaled = (int)dis.readShort();
  port = (int)dis.readUnsignedByte()&0x00FF;
  rssi = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+22];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.writeShort(chan1_scaled&0x00FFFF);
  dos.writeShort(chan2_scaled&0x00FFFF);
  dos.writeShort(chan3_scaled&0x00FFFF);
  dos.writeShort(chan4_scaled&0x00FFFF);
  dos.writeShort(chan5_scaled&0x00FFFF);
  dos.writeShort(chan6_scaled&0x00FFFF);
  dos.writeShort(chan7_scaled&0x00FFFF);
  dos.writeShort(chan8_scaled&0x00FFFF);
  dos.writeByte(port&0x00FF);
  dos.writeByte(rssi&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
