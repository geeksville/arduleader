/**
 * Generated class : msg_command_ack
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
 * Class msg_command_ack
 * Report status of a command. Includes feedback wether the command was executed.
 **/
public class msg_command_ack extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_COMMAND_ACK = 77;
  private static final long serialVersionUID = MAVLINK_MSG_ID_COMMAND_ACK;
  public msg_command_ack(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_COMMAND_ACK;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 3;
}

  /**
   * Command ID, as defined by MAV_CMD enum.
   */
  public int command;
  /**
   * See MAV_RESULT enum
   */
  public int result;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  command = (int)dis.getShort()&0x00FFFF;
  result = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+3];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(command&0x00FFFF));
  dos.put((byte)(result&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 3);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[9] = crcl;
  buffer[10] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_COMMAND_ACK : " +   "  command="+command+  "  result="+result;}
}
