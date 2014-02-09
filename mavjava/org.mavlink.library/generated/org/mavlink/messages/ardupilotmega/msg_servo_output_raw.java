/**
 * Generated class : msg_servo_output_raw
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
 * Class msg_servo_output_raw
 * The RAW values of the servo outputs (for RC input from the remote, use the RC_CHANNELS messages). The standard PPM modulation is as follows: 1000 microseconds: 0%, 2000 microseconds: 100%.
 **/
public class msg_servo_output_raw extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SERVO_OUTPUT_RAW = 36;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SERVO_OUTPUT_RAW;
  public msg_servo_output_raw(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SERVO_OUTPUT_RAW;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 21;
}

  /**
   * Timestamp (microseconds since system boot)
   */
  public long time_usec;
  /**
   * Servo output 1 value, in microseconds
   */
  public int servo1_raw;
  /**
   * Servo output 2 value, in microseconds
   */
  public int servo2_raw;
  /**
   * Servo output 3 value, in microseconds
   */
  public int servo3_raw;
  /**
   * Servo output 4 value, in microseconds
   */
  public int servo4_raw;
  /**
   * Servo output 5 value, in microseconds
   */
  public int servo5_raw;
  /**
   * Servo output 6 value, in microseconds
   */
  public int servo6_raw;
  /**
   * Servo output 7 value, in microseconds
   */
  public int servo7_raw;
  /**
   * Servo output 8 value, in microseconds
   */
  public int servo8_raw;
  /**
   * Servo output port (set of 8 outputs = 1 port). Most MAVs will just use one, but this allows to encode more than 8 servos.
   */
  public int port;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_usec = (int)dis.getInt()&0x00FFFFFFFF;
  servo1_raw = (int)dis.getShort()&0x00FFFF;
  servo2_raw = (int)dis.getShort()&0x00FFFF;
  servo3_raw = (int)dis.getShort()&0x00FFFF;
  servo4_raw = (int)dis.getShort()&0x00FFFF;
  servo5_raw = (int)dis.getShort()&0x00FFFF;
  servo6_raw = (int)dis.getShort()&0x00FFFF;
  servo7_raw = (int)dis.getShort()&0x00FFFF;
  servo8_raw = (int)dis.getShort()&0x00FFFF;
  port = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+21];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(time_usec&0x00FFFFFFFF));
  dos.putShort((short)(servo1_raw&0x00FFFF));
  dos.putShort((short)(servo2_raw&0x00FFFF));
  dos.putShort((short)(servo3_raw&0x00FFFF));
  dos.putShort((short)(servo4_raw&0x00FFFF));
  dos.putShort((short)(servo5_raw&0x00FFFF));
  dos.putShort((short)(servo6_raw&0x00FFFF));
  dos.putShort((short)(servo7_raw&0x00FFFF));
  dos.putShort((short)(servo8_raw&0x00FFFF));
  dos.put((byte)(port&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 21);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[27] = crcl;
  buffer[28] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SERVO_OUTPUT_RAW : " +   "  time_usec="+time_usec+  "  servo1_raw="+servo1_raw+  "  servo2_raw="+servo2_raw+  "  servo3_raw="+servo3_raw+  "  servo4_raw="+servo4_raw+  "  servo5_raw="+servo5_raw+  "  servo6_raw="+servo6_raw+  "  servo7_raw="+servo7_raw+  "  servo8_raw="+servo8_raw+  "  port="+port;}
}
