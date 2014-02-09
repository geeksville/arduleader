/**
 * Generated class : msg_ping
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
 * Class msg_ping
 * A ping message either requesting or responding to a ping. This allows to measure the system latencies, including serial port, radio modem and UDP connections.
 **/
public class msg_ping extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_PING = 4;
  private static final long serialVersionUID = MAVLINK_MSG_ID_PING;
  public msg_ping(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_PING;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 14;
}

  /**
   * Unix timestamp in microseconds
   */
  public long time_usec;
  /**
   * PING sequence
   */
  public long seq;
  /**
   * 0: request ping from all receiving systems, if greater than 0: message is a ping response and number is the system id of the requesting system
   */
  public int target_system;
  /**
   * 0: request ping from all receiving components, if greater than 0: message is a ping response and number is the system id of the requesting system
   */
  public int target_component;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (long)dis.getLong();
  seq = (int)dis.getInt()&0x00FFFFFFFF;
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
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
  dos.putLong(time_usec);
  dos.putInt((int)(seq&0x00FFFFFFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 14);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[20] = crcl;
  buffer[21] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_PING : " +   "  time_usec="+time_usec+  "  seq="+seq+  "  target_system="+target_system+  "  target_component="+target_component;}
}
