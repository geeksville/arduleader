/**
 * Generated class : msg_limits_status
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
 * Class msg_limits_status
 * Status of AP_Limits. Sent in extended
	    status stream when AP_Limits is enabled
 **/
public class msg_limits_status extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_LIMITS_STATUS = 167;
  private static final long serialVersionUID = MAVLINK_MSG_ID_LIMITS_STATUS;
  public msg_limits_status(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_LIMITS_STATUS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 22;
}

  /**
   * time of last breach in milliseconds since boot
   */
  public long last_trigger;
  /**
   * time of last recovery action in milliseconds since boot
   */
  public long last_action;
  /**
   * time of last successful recovery in milliseconds since boot
   */
  public long last_recovery;
  /**
   * time of last all-clear in milliseconds since boot
   */
  public long last_clear;
  /**
   * number of fence breaches
   */
  public int breach_count;
  /**
   * state of AP_Limits, (see enum LimitState, LIMITS_STATE)
   */
  public int limits_state;
  /**
   * AP_Limit_Module bitfield of enabled modules, (see enum moduleid or LIMIT_MODULE)
   */
  public int mods_enabled;
  /**
   * AP_Limit_Module bitfield of required modules, (see enum moduleid or LIMIT_MODULE)
   */
  public int mods_required;
  /**
   * AP_Limit_Module bitfield of triggered modules, (see enum moduleid or LIMIT_MODULE)
   */
  public int mods_triggered;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  last_trigger = (int)dis.getInt()&0x00FFFFFFFF;
  last_action = (int)dis.getInt()&0x00FFFFFFFF;
  last_recovery = (int)dis.getInt()&0x00FFFFFFFF;
  last_clear = (int)dis.getInt()&0x00FFFFFFFF;
  breach_count = (int)dis.getShort()&0x00FFFF;
  limits_state = (int)dis.get()&0x00FF;
  mods_enabled = (int)dis.get()&0x00FF;
  mods_required = (int)dis.get()&0x00FF;
  mods_triggered = (int)dis.get()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+22];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putInt((int)(last_trigger&0x00FFFFFFFF));
  dos.putInt((int)(last_action&0x00FFFFFFFF));
  dos.putInt((int)(last_recovery&0x00FFFFFFFF));
  dos.putInt((int)(last_clear&0x00FFFFFFFF));
  dos.putShort((short)(breach_count&0x00FFFF));
  dos.put((byte)(limits_state&0x00FF));
  dos.put((byte)(mods_enabled&0x00FF));
  dos.put((byte)(mods_required&0x00FF));
  dos.put((byte)(mods_triggered&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 22);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[28] = crcl;
  buffer[29] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_LIMITS_STATUS : " +   "  last_trigger="+last_trigger+  "  last_action="+last_action+  "  last_recovery="+last_recovery+  "  last_clear="+last_clear+  "  breach_count="+breach_count+  "  limits_state="+limits_state+  "  mods_enabled="+mods_enabled+  "  mods_required="+mods_required+  "  mods_triggered="+mods_triggered;}
}
