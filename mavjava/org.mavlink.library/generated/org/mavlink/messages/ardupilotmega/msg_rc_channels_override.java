/**
 * Generated class : msg_rc_channels_override
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
 * Class msg_rc_channels_override
 * The RAW values of the RC channels sent to the MAV to override info received from the RC radio. A value of -1 means no change to that channel. A value of 0 means control of that channel should be released back to the RC radio. The standard PPM modulation is as follows: 1000 microseconds: 0%, 2000 microseconds: 100%. Individual receivers/transmitters might violate this specification.
 **/
public class msg_rc_channels_override extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE = 70;
  private static final long serialVersionUID = MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE;
  public msg_rc_channels_override(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 18;
}

  /**
   * RC channel 1 value, in microseconds
   */
  public int chan1_raw;
  /**
   * RC channel 2 value, in microseconds
   */
  public int chan2_raw;
  /**
   * RC channel 3 value, in microseconds
   */
  public int chan3_raw;
  /**
   * RC channel 4 value, in microseconds
   */
  public int chan4_raw;
  /**
   * RC channel 5 value, in microseconds
   */
  public int chan5_raw;
  /**
   * RC channel 6 value, in microseconds
   */
  public int chan6_raw;
  /**
   * RC channel 7 value, in microseconds
   */
  public int chan7_raw;
  /**
   * RC channel 8 value, in microseconds
   */
  public int chan8_raw;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  chan1_raw = (int)dis.readUnsignedShort()&0x00FFFF;
  chan2_raw = (int)dis.readUnsignedShort()&0x00FFFF;
  chan3_raw = (int)dis.readUnsignedShort()&0x00FFFF;
  chan4_raw = (int)dis.readUnsignedShort()&0x00FFFF;
  chan5_raw = (int)dis.readUnsignedShort()&0x00FFFF;
  chan6_raw = (int)dis.readUnsignedShort()&0x00FFFF;
  chan7_raw = (int)dis.readUnsignedShort()&0x00FFFF;
  chan8_raw = (int)dis.readUnsignedShort()&0x00FFFF;
  target_system = (int)dis.readUnsignedByte()&0x00FF;
  target_component = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+18];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeShort(chan1_raw&0x00FFFF);
  dos.writeShort(chan2_raw&0x00FFFF);
  dos.writeShort(chan3_raw&0x00FFFF);
  dos.writeShort(chan4_raw&0x00FFFF);
  dos.writeShort(chan5_raw&0x00FFFF);
  dos.writeShort(chan6_raw&0x00FFFF);
  dos.writeShort(chan7_raw&0x00FFFF);
  dos.writeShort(chan8_raw&0x00FFFF);
  dos.writeByte(target_system&0x00FF);
  dos.writeByte(target_component&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 18);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[24] = crcl;
  buffer[25] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_RC_CHANNELS_OVERRIDE : " +   "  chan1_raw="+chan1_raw+  "  chan2_raw="+chan2_raw+  "  chan3_raw="+chan3_raw+  "  chan4_raw="+chan4_raw+  "  chan5_raw="+chan5_raw+  "  chan6_raw="+chan6_raw+  "  chan7_raw="+chan7_raw+  "  chan8_raw="+chan8_raw+  "  target_system="+target_system+  "  target_component="+target_component;}
}
