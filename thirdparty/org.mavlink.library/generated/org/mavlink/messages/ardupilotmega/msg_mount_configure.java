/**
 * Generated class : msg_mount_configure
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
 * Class msg_mount_configure
 * Message to configure a camera mount, directional antenna, etc.
 **/
public class msg_mount_configure extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_MOUNT_CONFIGURE = 156;
  private static final long serialVersionUID = MAVLINK_MSG_ID_MOUNT_CONFIGURE;
  public msg_mount_configure(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_MOUNT_CONFIGURE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 6;
}

  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
  /**
   * mount operating mode (see MAV_MOUNT_MODE enum)
   */
  public int mount_mode;
  /**
   * (1 = yes, 0 = no)
   */
  public int stab_roll;
  /**
   * (1 = yes, 0 = no)
   */
  public int stab_pitch;
  /**
   * (1 = yes, 0 = no)
   */
  public int stab_yaw;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  target_system = (int)dis.get()&0x00FF;
  target_component = (int)dis.get()&0x00FF;
  mount_mode = (int)dis.get()&0x00FF;
  stab_roll = (int)dis.get()&0x00FF;
  stab_pitch = (int)dis.get()&0x00FF;
  stab_yaw = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+6];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.put((byte)(target_system&0x00FF));
  dos.put((byte)(target_component&0x00FF));
  dos.put((byte)(mount_mode&0x00FF));
  dos.put((byte)(stab_roll&0x00FF));
  dos.put((byte)(stab_pitch&0x00FF));
  dos.put((byte)(stab_yaw&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 6);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[12] = crcl;
  buffer[13] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_MOUNT_CONFIGURE : " +   "  target_system="+target_system+  "  target_component="+target_component+  "  mount_mode="+mount_mode+  "  stab_roll="+stab_roll+  "  stab_pitch="+stab_pitch+  "  stab_yaw="+stab_yaw;}
}
