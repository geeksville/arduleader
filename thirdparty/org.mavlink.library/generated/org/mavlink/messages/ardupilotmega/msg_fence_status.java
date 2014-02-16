/**
 * Generated class : msg_fence_status
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
 * Class msg_fence_status
 * Status of geo-fencing. Sent in extended
	    status stream when fencing enabled
 **/
public class msg_fence_status extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_FENCE_STATUS = 162;
  private static final long serialVersionUID = MAVLINK_MSG_ID_FENCE_STATUS;
  public msg_fence_status(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_FENCE_STATUS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 8;
}

  /**
   * time of last breach in milliseconds since boot
   */
  public long breach_time;
  /**
   * number of fence breaches
   */
  public int breach_count;
  /**
   * 0 if currently inside fence, 1 if outside
   */
  public int breach_status;
  /**
   * last breach type (see FENCE_BREACH_* enum)
   */
  public int breach_type;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  breach_time = (int)dis.getInt()&0x00FFFFFFFF;
  breach_count = (int)dis.getShort()&0x00FFFF;
  breach_status = (int)dis.get()&0x00FF;
  breach_type = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+8];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(breach_time&0x00FFFFFFFF));
  dos.putShort((short)(breach_count&0x00FFFF));
  dos.put((byte)(breach_status&0x00FF));
  dos.put((byte)(breach_type&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 8);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[14] = crcl;
  buffer[15] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_FENCE_STATUS : " +   "  breach_time="+breach_time+  "  breach_count="+breach_count+  "  breach_status="+breach_status+  "  breach_type="+breach_type;}
}
