/**
 * Generated class : msg_vision_speed_estimate
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
 * Class msg_vision_speed_estimate
 * 
 **/
public class msg_vision_speed_estimate extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE = 103;
  private static final long serialVersionUID = MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE;
  public msg_vision_speed_estimate(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 20;
}

  /**
   * Timestamp (microseconds, synced to UNIX time or since system boot)
   */
  public long usec;
  /**
   * Global X speed
   */
  public float x;
  /**
   * Global Y speed
   */
  public float y;
  /**
   * Global Z speed
   */
  public float z;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  usec = (long)dis.getLong();
  x = (float)dis.getFloat();
  y = (float)dis.getFloat();
  z = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+20];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putLong(usec);
  dos.putFloat(x);
  dos.putFloat(y);
  dos.putFloat(z);
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 20);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[26] = crcl;
  buffer[27] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_VISION_SPEED_ESTIMATE : " +   "  usec="+usec+  "  x="+x+  "  y="+y+  "  z="+z;}
}
