/**
 * Generated class : msg_command_long
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
 * Class msg_command_long
 * Send a command with up to four parameters to the MAV
 **/
public class msg_command_long extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_COMMAND_LONG = 76;
  private static final long serialVersionUID = MAVLINK_MSG_ID_COMMAND_LONG;
  public msg_command_long(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_COMMAND_LONG;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 33;
}

  /**
   * Parameter 1, as defined by MAV_CMD enum.
   */
  public float param1;
  /**
   * Parameter 2, as defined by MAV_CMD enum.
   */
  public float param2;
  /**
   * Parameter 3, as defined by MAV_CMD enum.
   */
  public float param3;
  /**
   * Parameter 4, as defined by MAV_CMD enum.
   */
  public float param4;
  /**
   * Parameter 5, as defined by MAV_CMD enum.
   */
  public float param5;
  /**
   * Parameter 6, as defined by MAV_CMD enum.
   */
  public float param6;
  /**
   * Parameter 7, as defined by MAV_CMD enum.
   */
  public float param7;
  /**
   * Command ID, as defined by MAV_CMD enum.
   */
  public int command;
  /**
   * System which should execute the command
   */
  public int target_system;
  /**
   * Component which should execute the command, 0 for all components
   */
  public int target_component;
  /**
   * 0: First transmission of this command. 1-255: Confirmation transmissions (e.g. for kill command)
   */
  public int confirmation;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  param1 = (float)dis.getFloat();
  param2 = (float)dis.getFloat();
  param3 = (float)dis.getFloat();
  param4 = (float)dis.getFloat();
  param5 = (float)dis.getFloat();
  param6 = (float)dis.getFloat();
  param7 = (float)dis.getFloat();
  command = (int)dis.getShort()&0x00FFFF;
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  confirmation = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+33];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(param1);
  dos.putFloat(param2);
  dos.putFloat(param3);
  dos.putFloat(param4);
  dos.putFloat(param5);
  dos.putFloat(param6);
  dos.putFloat(param7);
  dos.putShort((short)(command&0x00FFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  dos.put((byte)(confirmation&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 33);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[39] = crcl;
  buffer[40] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_COMMAND_LONG : " +   "  param1="+param1+  "  param2="+param2+  "  param3="+param3+  "  param4="+param4+  "  param5="+param5+  "  param6="+param6+  "  param7="+param7+  "  command="+command+  "  target_system="+target_system+  "  target_component="+target_component+  "  confirmation="+confirmation;}
}
