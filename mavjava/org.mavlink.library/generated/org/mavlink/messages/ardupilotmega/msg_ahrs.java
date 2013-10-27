/**
 * Generated class : msg_ahrs
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
 * Class msg_ahrs
 * Status of DCM attitude estimator
 **/
public class msg_ahrs extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_AHRS = 163;
  private static final long serialVersionUID = MAVLINK_MSG_ID_AHRS;
  public msg_ahrs(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_AHRS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 28;
}

  /**
   * X gyro drift estimate rad/s
   */
  public float omegaIx;
  /**
   * Y gyro drift estimate rad/s
   */
  public float omegaIy;
  /**
   * Z gyro drift estimate rad/s
   */
  public float omegaIz;
  /**
   * average accel_weight
   */
  public float accel_weight;
  /**
   * average renormalisation value
   */
  public float renorm_val;
  /**
   * average error_roll_pitch value
   */
  public float error_rp;
  /**
   * average error_yaw value
   */
  public float error_yaw;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  omegaIx = (float)dis.getFloat();
  omegaIy = (float)dis.getFloat();
  omegaIz = (float)dis.getFloat();
  accel_weight = (float)dis.getFloat();
  renorm_val = (float)dis.getFloat();
  error_rp = (float)dis.getFloat();
  error_yaw = (float)dis.getFloat();
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
  dos.putFloat(omegaIx);
  dos.putFloat(omegaIy);
  dos.putFloat(omegaIz);
  dos.putFloat(accel_weight);
  dos.putFloat(renorm_val);
  dos.putFloat(error_rp);
  dos.putFloat(error_yaw);
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 28);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[34] = crcl;
  buffer[35] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_AHRS : " +   "  omegaIx="+omegaIx+  "  omegaIy="+omegaIy+  "  omegaIz="+omegaIz+  "  accel_weight="+accel_weight+  "  renorm_val="+renorm_val+  "  error_rp="+error_rp+  "  error_yaw="+error_yaw;}
}
