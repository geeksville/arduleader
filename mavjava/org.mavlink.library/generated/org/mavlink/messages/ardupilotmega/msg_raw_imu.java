/**
 * Generated class : msg_raw_imu
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
 * Class msg_raw_imu
 * The RAW IMU readings for the usual 9DOF sensor setup. This message should always contain the true raw values without any scaling to allow data capture and system debugging.
 **/
public class msg_raw_imu extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_RAW_IMU = 27;
  private static final long serialVersionUID = MAVLINK_MSG_ID_RAW_IMU;
  public msg_raw_imu(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_RAW_IMU;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 26;
}

  /**
   * Timestamp (microseconds since UNIX epoch or microseconds since system boot)
   */
  public long time_usec;
  /**
   * X acceleration (raw)
   */
  public int xacc;
  /**
   * Y acceleration (raw)
   */
  public int yacc;
  /**
   * Z acceleration (raw)
   */
  public int zacc;
  /**
   * Angular speed around X axis (raw)
   */
  public int xgyro;
  /**
   * Angular speed around Y axis (raw)
   */
  public int ygyro;
  /**
   * Angular speed around Z axis (raw)
   */
  public int zgyro;
  /**
   * X Magnetic field (raw)
   */
  public int xmag;
  /**
   * Y Magnetic field (raw)
   */
  public int ymag;
  /**
   * Z Magnetic field (raw)
   */
  public int zmag;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  time_usec = (long)dis.readLong();
  xacc = (int)dis.readShort();
  yacc = (int)dis.readShort();
  zacc = (int)dis.readShort();
  xgyro = (int)dis.readShort();
  ygyro = (int)dis.readShort();
  zgyro = (int)dis.readShort();
  xmag = (int)dis.readShort();
  ymag = (int)dis.readShort();
  zmag = (int)dis.readShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+26];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeLong(time_usec);
  dos.writeShort(xacc&0x00FFFF);
  dos.writeShort(yacc&0x00FFFF);
  dos.writeShort(zacc&0x00FFFF);
  dos.writeShort(xgyro&0x00FFFF);
  dos.writeShort(ygyro&0x00FFFF);
  dos.writeShort(zgyro&0x00FFFF);
  dos.writeShort(xmag&0x00FFFF);
  dos.writeShort(ymag&0x00FFFF);
  dos.writeShort(zmag&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 26);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[32] = crcl;
  buffer[33] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_RAW_IMU : " +   "  time_usec="+time_usec+  "  xacc="+xacc+  "  yacc="+yacc+  "  zacc="+zacc+  "  xgyro="+xgyro+  "  ygyro="+ygyro+  "  zgyro="+zgyro+  "  xmag="+xmag+  "  ymag="+ymag+  "  zmag="+zmag;}
}
