/**
 * Generated class : msg_param_value
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
 * Class msg_param_value
 * Emit the value of a onboard parameter. The inclusion of param_count and param_index in the message allows the recipient to keep track of received parameters and allows him to re-request missing parameters after a loss or timeout.
 **/
public class msg_param_value extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_PARAM_VALUE = 22;
  private static final long serialVersionUID = MAVLINK_MSG_ID_PARAM_VALUE;
  public msg_param_value(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_PARAM_VALUE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 25;
}

  /**
   * Onboard parameter value
   */
  public float param_value;
  /**
   * Total number of onboard parameters
   */
  public int param_count;
  /**
   * Index of this onboard parameter
   */
  public int param_index;
  /**
   * Onboard parameter id, terminated by NULL if the length is less than 16 human-readable chars and WITHOUT null termination (NULL) byte if the length is exactly 16 chars - applications have to provide 16+1 bytes storage if the ID is stored as string
   */
  public char[] param_id = new char[16];
  public void setParam_id(String tmp) {
    int len = Math.min(tmp.length(), 16);
    for (int i=0; i<len; i++) {
      param_id[i] = tmp.charAt(i);
    }
    for (int i=len; i<16; i++) {
      param_id[i] = 0;
    }
  }
  public String getParam_id() {
    String result="";
    for (int i=0; i<16; i++) {
      if (param_id[i] != 0) result=result+param_id[i]; else break;
    }
    return result;
  }
  /**
   * Onboard parameter type: see the MAV_PARAM_TYPE enum for supported data types.
   */
  public int param_type;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  param_value = (float)dis.getFloat();
  param_count = (int)dis.getShort()&0x00FFFF;
  param_index = (int)dis.getShort()&0x00FFFF;
  for (int i=0; i<16; i++) {
    param_id[i] = (char)dis.get();
  }
  param_type = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+25];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(param_value);
  dos.putShort((short)(param_count&0x00FFFF));
  dos.putShort((short)(param_index&0x00FFFF));
  for (int i=0; i<16; i++) {
    dos.put((byte)(param_id[i]));
  }
  dos.put((byte)(param_type&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 25);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[31] = crcl;
  buffer[32] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_PARAM_VALUE : " +   "  param_value="+param_value+  "  param_count="+param_count+  "  param_index="+param_index+  "  param_id="+getParam_id()+  "  param_type="+param_type;}
}
