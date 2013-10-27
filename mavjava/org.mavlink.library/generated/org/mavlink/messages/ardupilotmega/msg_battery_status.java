/**
 * Generated class : msg_battery_status
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
 * Class msg_battery_status
 * Transmitte battery informations for a accu pack.
 **/
public class msg_battery_status extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_BATTERY_STATUS = 147;
  private static final long serialVersionUID = MAVLINK_MSG_ID_BATTERY_STATUS;
  public msg_battery_status(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_BATTERY_STATUS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 16;
}

  /**
   * Battery voltage of cell 1, in millivolts (1 = 1 millivolt)
   */
  public int voltage_cell_1;
  /**
   * Battery voltage of cell 2, in millivolts (1 = 1 millivolt), -1: no cell
   */
  public int voltage_cell_2;
  /**
   * Battery voltage of cell 3, in millivolts (1 = 1 millivolt), -1: no cell
   */
  public int voltage_cell_3;
  /**
   * Battery voltage of cell 4, in millivolts (1 = 1 millivolt), -1: no cell
   */
  public int voltage_cell_4;
  /**
   * Battery voltage of cell 5, in millivolts (1 = 1 millivolt), -1: no cell
   */
  public int voltage_cell_5;
  /**
   * Battery voltage of cell 6, in millivolts (1 = 1 millivolt), -1: no cell
   */
  public int voltage_cell_6;
  /**
   * Battery current, in 10*milliamperes (1 = 10 milliampere), -1: autopilot does not measure the current
   */
  public int current_battery;
  /**
   * Accupack ID
   */
  public int accu_id;
  /**
   * Remaining battery energy: (0%: 0, 100%: 100), -1: autopilot does not estimate the remaining battery
   */
  public int battery_remaining;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  voltage_cell_1 = (int)dis.getShort()&0x00FFFF;
  voltage_cell_2 = (int)dis.getShort()&0x00FFFF;
  voltage_cell_3 = (int)dis.getShort()&0x00FFFF;
  voltage_cell_4 = (int)dis.getShort()&0x00FFFF;
  voltage_cell_5 = (int)dis.getShort()&0x00FFFF;
  voltage_cell_6 = (int)dis.getShort()&0x00FFFF;
  current_battery = (int)dis.getShort();
  accu_id = (int)dis.get()&0x00FF;
  battery_remaining = (int)dis.get();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+16];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(voltage_cell_1&0x00FFFF));
  dos.putShort((short)(voltage_cell_2&0x00FFFF));
  dos.putShort((short)(voltage_cell_3&0x00FFFF));
  dos.putShort((short)(voltage_cell_4&0x00FFFF));
  dos.putShort((short)(voltage_cell_5&0x00FFFF));
  dos.putShort((short)(voltage_cell_6&0x00FFFF));
  dos.putShort((short)(current_battery&0x00FFFF));
  dos.put((byte)(accu_id&0x00FF));
  dos.put((byte)(battery_remaining&0x00FF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 16);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[22] = crcl;
  buffer[23] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_BATTERY_STATUS : " +   "  voltage_cell_1="+voltage_cell_1+  "  voltage_cell_2="+voltage_cell_2+  "  voltage_cell_3="+voltage_cell_3+  "  voltage_cell_4="+voltage_cell_4+  "  voltage_cell_5="+voltage_cell_5+  "  voltage_cell_6="+voltage_cell_6+  "  current_battery="+current_battery+  "  accu_id="+accu_id+  "  battery_remaining="+battery_remaining;}
}
