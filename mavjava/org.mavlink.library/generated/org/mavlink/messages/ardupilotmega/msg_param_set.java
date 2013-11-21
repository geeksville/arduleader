/**
 * Generated class : msg_param_set
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
 * Class msg_param_set
 * Set a parameter value TEMPORARILY to RAM. It will be reset to default on system reboot. Send the ACTION MAV_ACTION_STORAGE_WRITE to PERMANENTLY write the RAM contents to EEPROM. IMPORTANT: The receiving component should acknowledge the new parameter value by sending a param_value message to all communication partners. This will also ensure that multiple GCS all have an up-to-date list of all parameters. If the sending GCS did not receive a PARAM_VALUE message within its timeout time, it should re-send the PARAM_SET message.
 **/
public class msg_param_set extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_PARAM_SET = 23;
  private static final long serialVersionUID = MAVLINK_MSG_ID_PARAM_SET;
  public msg_param_set(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_PARAM_SET;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 23;
}

  /**
   * Onboard parameter value
   */
  public float param_value;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
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
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  for (int i=0; i<16; i++) {
    param_id[i] = (char)dis.get();
  }
  param_type = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+23];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(param_value);
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  for (int i=0; i<16; i++) {
    dos.put((byte)(param_id[i]));
  }
  dos.put((byte)(param_type&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 23);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[29] = crcl;
  buffer[30] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_PARAM_SET : " +   "  param_value="+param_value+  "  target_system="+target_system+  "  target_component="+target_component+  "  param_id="+getParam_id()+  "  param_type="+param_type;}
}
