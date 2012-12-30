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
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
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
public void decode(LittleEndianDataInputStream dis) throws IOException {
  omegaIx = (float)dis.readFloat();
  omegaIy = (float)dis.readFloat();
  omegaIz = (float)dis.readFloat();
  accel_weight = (float)dis.readFloat();
  renorm_val = (float)dis.readFloat();
  error_rp = (float)dis.readFloat();
  error_yaw = (float)dis.readFloat();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+28];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(omegaIx);
  dos.writeFloat(omegaIy);
  dos.writeFloat(omegaIz);
  dos.writeFloat(accel_weight);
  dos.writeFloat(renorm_val);
  dos.writeFloat(error_rp);
  dos.writeFloat(error_yaw);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
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
