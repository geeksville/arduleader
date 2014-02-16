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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  press_abs = (float)dis.getFloat();
  press_diff = (float)dis.getFloat();
  temperature = (int)dis.getShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+14];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.putFloat(press_abs);
  dos.putFloat(press_diff);
  dos.putShort((short)(temperature&0x00FFFF));
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
