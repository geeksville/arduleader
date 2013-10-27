/**
 * Generated class : msg_sensor_offsets
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
 * Class msg_sensor_offsets
 * Offsets and calibrations values for hardware
        sensors. This makes it easier to debug the calibration process.
 **/
public class msg_sensor_offsets extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SENSOR_OFFSETS = 150;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SENSOR_OFFSETS;
  public msg_sensor_offsets(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SENSOR_OFFSETS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 42;
}

  /**
   * magnetic declination (radians)
   */
  public float mag_declination;
  /**
   * raw pressure from barometer
   */
  public long raw_press;
  /**
   * raw temperature from barometer
   */
  public long raw_temp;
  /**
   * gyro X calibration
   */
  public float gyro_cal_x;
  /**
   * gyro Y calibration
   */
  public float gyro_cal_y;
  /**
   * gyro Z calibration
   */
  public float gyro_cal_z;
  /**
   * accel X calibration
   */
  public float accel_cal_x;
  /**
   * accel Y calibration
   */
  public float accel_cal_y;
  /**
   * accel Z calibration
   */
  public float accel_cal_z;
  /**
   * magnetometer X offset
   */
  public int mag_ofs_x;
  /**
   * magnetometer Y offset
   */
  public int mag_ofs_y;
  /**
   * magnetometer Z offset
   */
  public int mag_ofs_z;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  mag_declination = (float)dis.getFloat();
  raw_press = (int)dis.getInt();
  raw_temp = (int)dis.getInt();
  gyro_cal_x = (float)dis.getFloat();
  gyro_cal_y = (float)dis.getFloat();
  gyro_cal_z = (float)dis.getFloat();
  accel_cal_x = (float)dis.getFloat();
  accel_cal_y = (float)dis.getFloat();
  accel_cal_z = (float)dis.getFloat();
  mag_ofs_x = (int)dis.getShort();
  mag_ofs_y = (int)dis.getShort();
  mag_ofs_z = (int)dis.getShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+42];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putFloat(mag_declination);
  dos.putInt((int)(raw_press&0x00FFFFFFFF));
  dos.putInt((int)(raw_temp&0x00FFFFFFFF));
  dos.putFloat(gyro_cal_x);
  dos.putFloat(gyro_cal_y);
  dos.putFloat(gyro_cal_z);
  dos.putFloat(accel_cal_x);
  dos.putFloat(accel_cal_y);
  dos.putFloat(accel_cal_z);
  dos.putShort((short)(mag_ofs_x&0x00FFFF));
  dos.putShort((short)(mag_ofs_y&0x00FFFF));
  dos.putShort((short)(mag_ofs_z&0x00FFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 42);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[48] = crcl;
  buffer[49] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SENSOR_OFFSETS : " +   "  mag_declination="+mag_declination+  "  raw_press="+raw_press+  "  raw_temp="+raw_temp+  "  gyro_cal_x="+gyro_cal_x+  "  gyro_cal_y="+gyro_cal_y+  "  gyro_cal_z="+gyro_cal_z+  "  accel_cal_x="+accel_cal_x+  "  accel_cal_y="+accel_cal_y+  "  accel_cal_z="+accel_cal_z+  "  mag_ofs_x="+mag_ofs_x+  "  mag_ofs_y="+mag_ofs_y+  "  mag_ofs_z="+mag_ofs_z;}
}
