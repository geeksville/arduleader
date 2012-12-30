/**
 * Generated class : msg_scaled_pressure
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
 * Class msg_scaled_pressure
 * The pressure readings for the typical setup of one absolute and differential pressure sensor. The units are as specified in each field.
 **/
public class msg_scaled_pressure extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SCALED_PRESSURE = 29;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SCALED_PRESSURE;
  public msg_scaled_pressure(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SCALED_PRESSURE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 14;
}

  /**
   * Timestamp (milliseconds since system boot)
   */
  public long time_boot_ms;
  /**
   * Absolute pressure (hectopascal)
   */
  public float press_abs;
  /**
   * Differential pressure 1 (hectopascal)
   */
  public float press_diff;
  /**
   * Temperature measurement (0.01 degrees celsius)
   */
  public int temperature;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_boot_ms = (int)dis.readInt()&0x00FFFFFFFF;
  press_abs = (float)dis.readFloat();
  press_diff = (float)dis.readFloat();
  temperature = (int)dis.readShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+14];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.writeFloat(press_abs);
  dos.writeFloat(press_diff);
  dos.writeShort(temperature&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 14);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[20] = crcl;
  buffer[21] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SCALED_PRESSURE : " +   "  time_boot_ms="+time_boot_ms+  "  press_abs="+press_abs+  "  press_diff="+press_diff+  "  temperature="+temperature;}
}
