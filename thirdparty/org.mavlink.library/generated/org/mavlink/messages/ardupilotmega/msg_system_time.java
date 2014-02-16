/**
 * Generated class : msg_system_time
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
 * Class msg_system_time
 * The system time is the time of the master clock, typically the computer clock of the main onboard computer.
 **/
public class msg_system_time extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SYSTEM_TIME = 2;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SYSTEM_TIME;
  public msg_system_time(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SYSTEM_TIME;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 12;
}

  /**
   * Timestamp of the master clock in microseconds since UNIX epoch.
   */
  public long time_unix_usec;
  /**
   * Timestamp of the component clock since boot time in milliseconds.
   */
  public long time_boot_ms;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_unix_usec = (long)dis.getLong();
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+12];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(time_unix_usec);
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 12);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[18] = crcl;
  buffer[19] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SYSTEM_TIME : " +   "  time_unix_usec="+time_unix_usec+  "  time_boot_ms="+time_boot_ms;}
}
