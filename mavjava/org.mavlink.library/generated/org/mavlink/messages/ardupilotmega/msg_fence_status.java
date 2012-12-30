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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  breach_time = (int)dis.readInt()&0x00FFFFFFFF;
  breach_count = (int)dis.readUnsignedShort()&0x00FFFF;
  breach_status = (int)dis.readUnsignedByte()&0x00FF;
  breach_type = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+8];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeInt((int)(breach_time&0x00FFFFFFFF));
  dos.writeShort(breach_count&0x00FFFF);
  dos.writeByte(breach_status&0x00FF);
  dos.writeByte(breach_type&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
