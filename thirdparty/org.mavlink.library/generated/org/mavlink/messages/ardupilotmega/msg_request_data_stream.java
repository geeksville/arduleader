/**
 * Generated class : msg_request_data_stream
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
 * Class msg_request_data_stream
 * 
 **/
public class msg_request_data_stream extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_REQUEST_DATA_STREAM = 66;
  private static final long serialVersionUID = MAVLINK_MSG_ID_REQUEST_DATA_STREAM;
  public msg_request_data_stream(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_REQUEST_DATA_STREAM;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 6;
}

  /**
   * The requested interval between two messages of this type
   */
  public int req_message_rate;
  /**
   * The target requested to send the message stream.
   */
  public int target_system;
  /**
   * The target requested to send the message stream.
   */
  public int target_component;
  /**
   * The ID of the requested data stream
   */
  public int req_stream_id;
  /**
   * 1 to start sending, 0 to stop sending.
   */
  public int start_stop;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  req_message_rate = (int)dis.getShort()&0x00FFFF;
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  req_stream_id = (int)dis.get()&0x00FF;
  start_stop = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+6];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(req_message_rate&0x00FFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  dos.put((byte)(req_stream_id&0x00FF));
  dos.put((byte)(start_stop&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 6);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[12] = crcl;
  buffer[13] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_REQUEST_DATA_STREAM : " +   "  req_message_rate="+req_message_rate+  "  target_system="+target_system+  "  target_component="+target_component+  "  req_stream_id="+req_stream_id+  "  start_stop="+start_stop;}
}
