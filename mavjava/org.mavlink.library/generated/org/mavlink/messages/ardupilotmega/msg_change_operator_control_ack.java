/**
 * Generated class : msg_change_operator_control_ack
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
 * Class msg_change_operator_control_ack
 * Accept / deny control of this MAV
 **/
public class msg_change_operator_control_ack extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL_ACK = 6;
  private static final long serialVersionUID = MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL_ACK;
  public msg_change_operator_control_ack(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL_ACK;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 3;
}

  /**
   * ID of the GCS this message
   */
  public int gcs_system_id;
  /**
   * 0: request control of this MAV, 1: Release control of this MAV
   */
  public int control_request;
  /**
   * 0: ACK, 1: NACK: Wrong passkey, 2: NACK: Unsupported passkey encryption method, 3: NACK: Already under control
   */
  public int ack;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  gcs_system_id = (int)dis.readUnsignedByte()&0x00FF;
  control_request = (int)dis.readUnsignedByte()&0x00FF;
  ack = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+3];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeByte(gcs_system_id&0x00FF);
  dos.writeByte(control_request&0x00FF);
  dos.writeByte(ack&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 3);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[9] = crcl;
  buffer[10] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_CHANGE_OPERATOR_CONTROL_ACK : " +   "  gcs_system_id="+gcs_system_id+  "  control_request="+control_request+  "  ack="+ack;}
}
