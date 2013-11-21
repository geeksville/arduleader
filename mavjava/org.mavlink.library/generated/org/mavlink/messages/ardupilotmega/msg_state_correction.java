/**
 * Generated class : msg_state_correction
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
 * Class msg_state_correction
 * Corrects the systems state by adding an error correction term to the position and velocity, and by rotating the attitude by a correction angle.
 **/
public class msg_state_correction extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_STATE_CORRECTION = 64;
  private static final long serialVersionUID = MAVLINK_MSG_ID_STATE_CORRECTION;
  public msg_state_correction(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_STATE_CORRECTION;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 36;
}

  /**
   * x position error
   */
  public float xErr;
  /**
   * y position error
   */
  public float yErr;
  /**
   * z position error
   */
  public float zErr;
  /**
   * roll error (radians)
   */
  public float rollErr;
  /**
   * pitch error (radians)
   */
  public float pitchErr;
  /**
   * yaw error (radians)
   */
  public float yawErr;
  /**
   * x velocity
   */
  public float vxErr;
  /**
   * y velocity
   */
  public float vyErr;
  /**
   * z velocity
   */
  public float vzErr;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  xErr = (float)dis.getFloat();
  yErr = (float)dis.getFloat();
  zErr = (float)dis.getFloat();
  rollErr = (float)dis.getFloat();
  pitchErr = (float)dis.getFloat();
  yawErr = (float)dis.getFloat();
  vxErr = (float)dis.getFloat();
  vyErr = (float)dis.getFloat();
  vzErr = (float)dis.getFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+36];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(xErr);
  dos.putFloat(yErr);
  dos.putFloat(zErr);
  dos.putFloat(rollErr);
  dos.putFloat(pitchErr);
  dos.putFloat(yawErr);
  dos.putFloat(vxErr);
  dos.putFloat(vyErr);
  dos.putFloat(vzErr);
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 36);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[42] = crcl;
  buffer[43] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_STATE_CORRECTION : " +   "  xErr="+xErr+  "  yErr="+yErr+  "  zErr="+zErr+  "  rollErr="+rollErr+  "  pitchErr="+pitchErr+  "  yawErr="+yawErr+  "  vxErr="+vxErr+  "  vyErr="+vyErr+  "  vzErr="+vzErr;}
}
