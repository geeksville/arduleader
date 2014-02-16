/**
 * Generated class : msg_heartbeat
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
 * Class msg_heartbeat
 * The heartbeat message shows that a system is present and responding. The type of the MAV and Autopilot hardware allow the receiving system to treat further messages from this system appropriate (e.g. by laying out the user interface based on the autopilot).
 **/
public class msg_heartbeat extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_HEARTBEAT = 0;
  private static final long serialVersionUID = MAVLINK_MSG_ID_HEARTBEAT;
  public msg_heartbeat(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_HEARTBEAT;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 9;
}

  /**
   * A bitfield for use for autopilot-specific flags.
   */
  public long custom_mode;
  /**
   * Type of the MAV (quadrotor, helicopter, etc., up to 15 types, defined in MAV_TYPE ENUM)
   */
  public int type;
  /**
   * Autopilot type / class. defined in MAV_AUTOPILOT ENUM
   */
  public int autopilot;
  /**
   * System mode bitfield, see MAV_MODE_FLAGS ENUM in mavlink/include/mavlink_types.h
   */
  public int base_mode;
  /**
   * System status flag, see MAV_STATE ENUM
   */
  public int system_status;
  /**
   * MAVLink version, not writable by user, gets added by protocol because of magic data type: uint8_t_mavlink_version
   */
  public int mavlink_version;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  custom_mode = (int)dis.getInt()&0x00FFFFFFFF;
  type = (int)dis.get()&0x00FF;
  autopilot = (int)dis.get()&0x00FF;
  base_mode = (int)dis.get()&0x00FF;
  system_status = (int)dis.get()&0x00FF;
  mavlink_version = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+9];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(custom_mode&0x00FFFFFFFF));
  dos.put((byte)(type&0x00FF));
  dos.put((byte)(autopilot&0x00FF));
  dos.put((byte)(base_mode&0x00FF));
  dos.put((byte)(system_status&0x00FF));
  dos.put((byte)(mavlink_version&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 9);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[15] = crcl;
  buffer[16] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_HEARTBEAT : " +   "  custom_mode="+custom_mode+  "  type="+type+  "  autopilot="+autopilot+  "  base_mode="+base_mode+  "  system_status="+system_status+  "  mavlink_version="+mavlink_version;}
}
