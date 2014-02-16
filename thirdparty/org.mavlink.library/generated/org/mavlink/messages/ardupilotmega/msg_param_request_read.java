/**
 * Generated class : msg_param_request_read
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
 * Class msg_param_request_read
 * Request to read the onboard parameter with the param_id string id. Onboard parameters are stored as key[const char*] -> value[float]. This allows to send a parameter to any other component (such as the GCS) without the need of previous knowledge of possible parameter names. Thus the same GCS can store different parameters for different autopilots. See also http://qgroundcontrol.org/parameter_interface for a full documentation of QGroundControl and IMU code.
 **/
public class msg_param_request_read extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_PARAM_REQUEST_READ = 20;
  private static final long serialVersionUID = MAVLINK_MSG_ID_PARAM_REQUEST_READ;
  public msg_param_request_read(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_PARAM_REQUEST_READ;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 20;
}

  /**
   * Parameter index. Send -1 to use the param ID field as identifier (else the param id will be ignored)
   */
  public int param_index;
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
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  param_index = (int)dis.getShort();
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  for (int i=0; i<16; i++) {
    param_id[i] = (char)dis.get();
  }
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
  dos.putShort((short)(param_index&0x00FFFF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  for (int i=0; i<16; i++) {
    dos.put((byte)(param_id[i]));
  }
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 20);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[26] = crcl;
  buffer[27] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_PARAM_REQUEST_READ : " +   "  param_index="+param_index+  "  target_system="+target_system+  "  target_component="+target_component+  "  param_id="+getParam_id();}
}
