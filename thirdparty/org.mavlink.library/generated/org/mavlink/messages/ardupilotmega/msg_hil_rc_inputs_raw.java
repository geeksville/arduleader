/**
 * Generated class : msg_hil_rc_inputs_raw
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
 * Class msg_hil_rc_inputs_raw
 * Sent from simulation to autopilot. The RAW values of the RC channels received. The standard PPM modulation is as follows: 1000 microseconds: 0%, 2000 microseconds: 100%. Individual receivers/transmitters might violate this specification.
 **/
public class msg_hil_rc_inputs_raw extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW = 92;
  private static final long serialVersionUID = MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW;
  public msg_hil_rc_inputs_raw(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 33;
}

  /**
   * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
   */
  public long time_usec;
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
   * RC channel 9 value, in microseconds
   */
  public int chan9_raw;
  /**
   * RC channel 10 value, in microseconds
   */
  public int chan10_raw;
  /**
   * RC channel 11 value, in microseconds
   */
  public int chan11_raw;
  /**
   * RC channel 12 value, in microseconds
   */
  public int chan12_raw;
  /**
   * Receive signal strength indicator, 0: 0%, 255: 100%
   */
  public int rssi;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (long)dis.getLong();
  chan1_raw = (int)dis.getShort()&0x00FFFF;
  chan2_raw = (int)dis.getShort()&0x00FFFF;
  chan3_raw = (int)dis.getShort()&0x00FFFF;
  chan4_raw = (int)dis.getShort()&0x00FFFF;
  chan5_raw = (int)dis.getShort()&0x00FFFF;
  chan6_raw = (int)dis.getShort()&0x00FFFF;
  chan7_raw = (int)dis.getShort()&0x00FFFF;
  chan8_raw = (int)dis.getShort()&0x00FFFF;
  chan9_raw = (int)dis.getShort()&0x00FFFF;
  chan10_raw = (int)dis.getShort()&0x00FFFF;
  chan11_raw = (int)dis.getShort()&0x00FFFF;
  chan12_raw = (int)dis.getShort()&0x00FFFF;
  rssi = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+33];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_usec);
  dos.putShort((short)(chan1_raw&0x00FFFF));
  dos.putShort((short)(chan2_raw&0x00FFFF));
  dos.putShort((short)(chan3_raw&0x00FFFF));
  dos.putShort((short)(chan4_raw&0x00FFFF));
  dos.putShort((short)(chan5_raw&0x00FFFF));
  dos.putShort((short)(chan6_raw&0x00FFFF));
  dos.putShort((short)(chan7_raw&0x00FFFF));
  dos.putShort((short)(chan8_raw&0x00FFFF));
  dos.putShort((short)(chan9_raw&0x00FFFF));
  dos.putShort((short)(chan10_raw&0x00FFFF));
  dos.putShort((short)(chan11_raw&0x00FFFF));
  dos.putShort((short)(chan12_raw&0x00FFFF));
  dos.put((byte)(rssi&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 33);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[39] = crcl;
  buffer[40] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_HIL_RC_INPUTS_RAW : " +   "  time_usec="+time_usec+  "  chan1_raw="+chan1_raw+  "  chan2_raw="+chan2_raw+  "  chan3_raw="+chan3_raw+  "  chan4_raw="+chan4_raw+  "  chan5_raw="+chan5_raw+  "  chan6_raw="+chan6_raw+  "  chan7_raw="+chan7_raw+  "  chan8_raw="+chan8_raw+  "  chan9_raw="+chan9_raw+  "  chan10_raw="+chan10_raw+  "  chan11_raw="+chan11_raw+  "  chan12_raw="+chan12_raw+  "  rssi="+rssi;}
}
