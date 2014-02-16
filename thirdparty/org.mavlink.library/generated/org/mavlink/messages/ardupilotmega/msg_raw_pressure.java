/**
 * Generated class : msg_raw_pressure
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
 * Class msg_raw_pressure
 * The RAW pressure readings for the typical setup of one absolute pressure and one differential pressure sensor. The sensor values should be the raw, UNSCALED ADC values.
 **/
public class msg_raw_pressure extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_RAW_PRESSURE = 28;
  private static final long serialVersionUID = MAVLINK_MSG_ID_RAW_PRESSURE;
  public msg_raw_pressure(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_RAW_PRESSURE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 16;
}

  /**
   * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
   */
  public long time_usec;
  /**
   * Absolute pressure (raw)
   */
  public int press_abs;
  /**
   * Differential pressure 1 (raw)
   */
  public int press_diff1;
  /**
   * Differential pressure 2 (raw)
   */
  public int press_diff2;
  /**
   * Raw Temperature measurement (raw)
   */
  public int temperature;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (long)dis.getLong();
  press_abs = (int)dis.getShort();
  press_diff1 = (int)dis.getShort();
  press_diff2 = (int)dis.getShort();
  temperature = (int)dis.getShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+16];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_usec);
  dos.putShort((short)(press_abs&0x00FFFF));
  dos.putShort((short)(press_diff1&0x00FFFF));
  dos.putShort((short)(press_diff2&0x00FFFF));
  dos.putShort((short)(temperature&0x00FFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 16);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[22] = crcl;
  buffer[23] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_RAW_PRESSURE : " +   "  time_usec="+time_usec+  "  press_abs="+press_abs+  "  press_diff1="+press_diff1+  "  press_diff2="+press_diff2+  "  temperature="+temperature;}
}
