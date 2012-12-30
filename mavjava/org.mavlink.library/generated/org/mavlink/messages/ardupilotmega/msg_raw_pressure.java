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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_usec = (long)dis.readLong();
  press_abs = (int)dis.readShort();
  press_diff1 = (int)dis.readShort();
  press_diff2 = (int)dis.readShort();
  temperature = (int)dis.readShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+16];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(time_usec);
  dos.writeShort(press_abs&0x00FFFF);
  dos.writeShort(press_diff1&0x00FFFF);
  dos.writeShort(press_diff2&0x00FFFF);
  dos.writeShort(temperature&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
