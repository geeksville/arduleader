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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public void decode(ByteBuffer dis) throws IOException {
  gcs_system_id = (int)dis.get()&0x00FF;
  control_request = (int)dis.get()&0x00FF;
  ack = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+3];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.put((byte)(gcs_system_id&0x00FF));
  dos.put((byte)(control_request&0x00FF));
  dos.put((byte)(ack&0x00FF));
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
