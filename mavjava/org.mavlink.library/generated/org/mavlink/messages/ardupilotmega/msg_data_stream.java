/**
 * Generated class : msg_data_stream
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
 * Class msg_data_stream
 * 
 **/
public class msg_data_stream extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_DATA_STREAM = 67;
  private static final long serialVersionUID = MAVLINK_MSG_ID_DATA_STREAM;
  public msg_data_stream(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_DATA_STREAM;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 4;
}

  /**
   * The requested interval between two messages of this type
   */
  public int message_rate;
  /**
   * The ID of the requested data stream
   */
  public int stream_id;
  /**
   * 1 stream is enabled, 0 stream is stopped.
   */
  public int on_off;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  message_rate = (int)dis.getShort()&0x00FFFF;
  stream_id = (int)dis.get()&0x00FF;
  on_off = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+4];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(message_rate&0x00FFFF));
  dos.put((byte)(stream_id&0x00FF));
  dos.put((byte)(on_off&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 4);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[10] = crcl;
  buffer[11] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_DATA_STREAM : " +   "  message_rate="+message_rate+  "  stream_id="+stream_id+  "  on_off="+on_off;}
}
