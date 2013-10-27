/**
 * Generated class : msg_change_operator_control
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
 * Class msg_change_operator_control
 * Request to control this MAV
 **/
public class msg_change_operator_control extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL = 5;
  private static final long serialVersionUID = MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL;
  public msg_change_operator_control(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 28;
}

  /**
   * System the GCS requests control for
   */
  public int target_system;
  /**
   * 0: request control of this MAV, 1: Release control of this MAV
   */
  public int control_request;
  /**
   * 0: key as plaintext, 1-255: future, different hashing/encryption variants. The GCS should in general use the safest mode possible initially and then gradually move down the encryption level if it gets a NACK message indicating an encryption mismatch.
   */
  public int version;
  /**
   * Password / Key, depending on version plaintext or encrypted. 25 or less characters, NULL terminated. The characters may involve A-Z, a-z, 0-9, and "!?,.-"
   */
  public char[] passkey = new char[25];
  public void setPasskey(String tmp) {
    int len = Math.min(tmp.length(), 25);
    for (int i=0; i<len; i++) {
      passkey[i] = tmp.charAt(i);
    }
    for (int i=len; i<25; i++) {
      passkey[i] = 0;
    }
  }
  public String getPasskey() {
    String result="";
    for (int i=0; i<25; i++) {
      if (passkey[i] != 0) result=result+passkey[i]; else break;
    }
    return result;
  }
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  target_system = (int)dis.get()&0x00FF;
  control_request = (int)dis.get()&0x00FF;
  version = (int)dis.get()&0x00FF;
  for (int i=0; i<25; i++) {
    passkey[i] = (char)dis.get();
  }
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+28];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(control_request&0x00FF));
  dos.put((byte)(version&0x00FF));
  for (int i=0; i<25; i++) {
    dos.put((byte)(passkey[i]));
  }
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 28);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[34] = crcl;
  buffer[35] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL : " +   "  target_system="+target_system+  "  control_request="+control_request+  "  version="+version+  "  passkey="+getPasskey();}
}
