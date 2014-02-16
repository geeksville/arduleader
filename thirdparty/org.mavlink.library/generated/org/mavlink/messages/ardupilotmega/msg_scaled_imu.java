/**
 * Generated class : msg_scaled_imu
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
 * Class msg_scaled_imu
 * The RAW IMU readings for the usual 9DOF sensor setup. This message should contain the scaled values to the described units
 **/
public class msg_scaled_imu extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SCALED_IMU = 26;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SCALED_IMU;
  public msg_scaled_imu(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SCALED_IMU;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 22;
}

  /**
   * Timestamp (milliseconds since system boot)
   */
  public long time_boot_ms;
  /**
   * X acceleration (mg)
   */
  public int xacc;
  /**
   * Y acceleration (mg)
   */
  public int yacc;
  /**
   * Z acceleration (mg)
   */
  public int zacc;
  /**
   * Angular speed around X axis (millirad /sec)
   */
  public int xgyro;
  /**
   * Angular speed around Y axis (millirad /sec)
   */
  public int ygyro;
  /**
   * Angular speed around Z axis (millirad /sec)
   */
  public int zgyro;
  /**
   * X Magnetic field (milli tesla)
   */
  public int xmag;
  /**
   * Y Magnetic field (milli tesla)
   */
  public int ymag;
  /**
   * Z Magnetic field (milli tesla)
   */
  public int zmag;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  time_boot_ms = (int)dis.getInt()&0x00FFFFFFFF;
  xacc = (int)dis.getShort();
  yacc = (int)dis.getShort();
  zacc = (int)dis.getShort();
  xgyro = (int)dis.getShort();
  ygyro = (int)dis.getShort();
  zgyro = (int)dis.getShort();
  xmag = (int)dis.getShort();
  ymag = (int)dis.getShort();
  zmag = (int)dis.getShort();
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
  dos.putInt((int)(time_boot_ms&0x00FFFFFFFF));
  dos.putShort((short)(xacc&0x00FFFF));
  dos.putShort((short)(yacc&0x00FFFF));
  dos.putShort((short)(zacc&0x00FFFF));
  dos.putShort((short)(xgyro&0x00FFFF));
  dos.putShort((short)(ygyro&0x00FFFF));
  dos.putShort((short)(zgyro&0x00FFFF));
  dos.putShort((short)(xmag&0x00FFFF));
  dos.putShort((short)(ymag&0x00FFFF));
  dos.putShort((short)(zmag&0x00FFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 22);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[28] = crcl;
  buffer[29] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SCALED_IMU : " +   "  time_boot_ms="+time_boot_ms+  "  xacc="+xacc+  "  yacc="+yacc+  "  zacc="+zacc+  "  xgyro="+xgyro+  "  ygyro="+ygyro+  "  zgyro="+zgyro+  "  xmag="+xmag+  "  ymag="+ymag+  "  zmag="+zmag;}
}
